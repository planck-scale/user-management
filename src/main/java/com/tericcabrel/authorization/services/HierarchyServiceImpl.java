package com.tericcabrel.authorization.services;

import com.tericcabrel.authorization.models.dtos.CreateGroupMemberDto;
import com.tericcabrel.authorization.models.entities.Group;
import com.tericcabrel.authorization.models.entities.User;
import com.tericcabrel.authorization.repositories.GroupRepository;
import com.tericcabrel.authorization.repositories.UserRepository;
import com.tericcabrel.authorization.services.interfaces.HierarchyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HierarchyServiceImpl implements HierarchyService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongodb;

    @Override
    public Group createGroup(String groupName, String parentName) {
        String slug = groupName.toLowerCase().replace(" ", "-");
        Group group;
        String path;
        String parentId;

        if (StringUtils.hasLength(parentName)) {
            Optional <Group> parentGroup = groupRepository.findByName(parentName);
            group = parentGroup.orElseThrow(() -> new IllegalArgumentException("Parent group not found"));
            path = group.getPath() + slug + "/";
            parentId = group.getId();
        } else {
            path = "/" + slug + "/";
            parentId = null;
        }

        Group newGroup = new Group();
        newGroup.setName(groupName);
        newGroup.setSlug(slug);
        newGroup.setPath(path);
        newGroup.setParentId(parentId);

        Group created = groupRepository.save(newGroup);
        log.debug("created {}", created);
        return created;
    }

    public Group getGroup(String groupName) {
        return groupRepository.findByName(groupName).orElseThrow(()
                -> new IllegalArgumentException("Group not found"));
    }
    @Override
    public User addUserToGroup(CreateGroupMemberDto member) {

        User user = userRepository.findByEmail(member.getEmail()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Group group = groupRepository.findByName(member.getGroup()).orElseThrow(() -> new IllegalArgumentException("Group not found"));

        log.debug("loaded user {}", user);
        log.debug("loaded group {}", group);
        List<String> pathsToAddToUser = new ArrayList<>();
        String[] pathSegments = group.getPath().split("/");
        String currentPath = "/";

        // Iterate through each segment to build the full set of ancestor paths.
        // For example, for path "/executive/engineering/backend/", this will produce:
        // ["/executive/", "/executive/engineering/", "/executive/engineering/backend/"]
        for (String segment : pathSegments) {
            // Skip any empty segments that might result from splitting the path string.
            if (StringUtils.hasText(segment)) {
                currentPath += segment + "/";
                pathsToAddToUser.add(currentPath);
            }
        }
        log.debug("paths to be added {}", pathsToAddToUser);
        Query query = new Query(Criteria.where("id").is(user.getId()));
        Update update = new Update().addToSet("groupPaths").each(pathsToAddToUser.toArray());

        return mongodb.findAndModify(query, update, User.class);
    }

    @Override
    public List<User> findUsersInSubtree(String groupId) {

        Group group = groupRepository.findByName(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));
        String pathRegex = "^" + group.getPath();
        return userRepository.findByGroupPathRegex(pathRegex);
    }

    @Override
    public List<User> findSiblings(String email) {
        // --- Step 1: Find the user and their specific group paths. ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + email));

        List<String> userGroupPaths = user.getGroupPaths();
        if (userGroupPaths == null || userGroupPaths.isEmpty()) {
            return new ArrayList<>();
        }

        // --- Step 2: Determine the user's longest path(s) and their parent paths. ---
        List<String> mostSpecificUserPaths = userGroupPaths.stream()
                .filter(path -> userGroupPaths.stream()
                        .noneMatch(otherPath -> !otherPath.equals(path) && otherPath.startsWith(path + "/")))
                .collect(Collectors.toList());

        // Get the parent paths for each most specific group.
        List<String> parentPaths = mostSpecificUserPaths.stream()
                .map(path -> {
                    int lastSlashIndex = path.substring(0, path.length() - 1).lastIndexOf('/');
                    return path.substring(0, lastSlashIndex + 1);
                })
                .distinct()
                .collect(Collectors.toList());

        // --- Step 3: Build and execute an aggregation pipeline to find siblings. ---
        // This pipeline ensures we find users with the same parent(s) and are at the same depth.
        var aggregation = Aggregation.newAggregation(
                // Stage 1: Exclude the user themselves.
                Aggregation.match(Criteria.where("_id").ne(user.getId())),
                // Stage 2: Filter for users whose `groupPaths` contain any of the parent paths.
                Aggregation.match(Criteria.where("groupPaths").in(parentPaths)),
                // Stage 3: Filter for users whose `groupPaths` array length matches the original user's.
                Aggregation.match(Criteria.where("groupPaths").size(user.getGroupPaths().size()))
        );

        // --- Step 4: Execute the aggregation and return the results. ---
        return mongodb.aggregate(aggregation, "users", User.class).getMappedResults();
    }

    @Override
    public List<User> findChildren(String email) {
        // --- Step 1: Find the user's most specific (longest) group paths. ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        List<String> userGroupPaths = user.getGroupPaths();
        if (userGroupPaths == null || userGroupPaths.isEmpty()) {
            return new ArrayList<>();
        }

        // Filter for the most specific paths.
        List<String> mostSpecificUserPaths = userGroupPaths.stream()
                .filter(path -> userGroupPaths.stream()
                        .noneMatch(otherPath -> !otherPath.equals(path) && otherPath.startsWith(path + "/")))
                .collect(Collectors.toList());

        // --- Step 2: Query for immediate child groups based on these specific paths. ---
        Set<String> childPathRegexes = mostSpecificUserPaths.stream()
                .map(path -> "^" + path.replace("/", "\\/") + "[^/]+\\/$")
                .collect(Collectors.toSet());

        if (childPathRegexes.isEmpty()) {
            return new ArrayList<>();
        }
        log.debug("children will be found in paths {}", childPathRegexes);
        Query groupQuery = new Query();
        List<Criteria> childPathCriteria = childPathRegexes.stream()
                .map(regex -> Criteria.where("path").regex(regex))
                .collect(Collectors.toList());
        groupQuery.addCriteria(new Criteria().orOperator(childPathCriteria));

        List<Group> immediateChildGroups = mongodb.find(groupQuery, Group.class);
        if (immediateChildGroups.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> immediateChildGroupPaths = immediateChildGroups.stream()
                .map(Group::getPath)
                .collect(Collectors.toList());
        log.debug("children will be found in groups {}", immediateChildGroupPaths);
        // --- Step 3: Find users whose most specific path is one of the child group paths. ---
        // A user's "most specific path" can be found by finding their longest group path.
        // This is a more involved query that requires aggregation.
        // Using `mongoTemplate` with a more complex aggregation pipeline is necessary here.
        return findUsersByMostSpecificPathIn(immediateChildGroupPaths);
    }

    /**
     * Helper method to find users whose longest (most specific) group path
     * is contained within a given set of paths.
     *
     * @param targetPaths The set of paths to match against.
     * @return A list of unique users who belong exclusively to these target paths.
     */
    private List<User> findUsersByMostSpecificPathIn(List<String> targetPaths) {
        // Build the aggregation pipeline.
        var aggregation = Aggregation.newAggregation(
                // Unwind the groupPaths array to process each path individually.
                Aggregation.unwind("groupPaths"),
                // Sort by the length of the path in descending order.
                Aggregation.sort(Sort.Direction.DESC, "groupPaths"),
                // Group by user ID and take the first (longest) path.
                Aggregation.group("_id")
                        .first("groupPaths").as("longestPath"),
                // Match documents where the longest path is in our list of immediate child paths.
                Aggregation.match(
                        Criteria.where("longestPath").in(targetPaths)
                ),
                // Look up the full user document based on the user ID.
                Aggregation.lookup("users", "_id", "_id", "fullUser"),
                // Unwind the fullUser array.
                Aggregation.unwind("fullUser"),
                // Replace the root with the full user document.
                Aggregation.replaceRoot("fullUser")
        );

        // Execute the aggregation and map the results to the User class.
        var results = mongodb.aggregate(aggregation, "users", User.class);
        return results.getMappedResults();
    }
}
