package com.tericcabrel.authorization.services;

import com.tericcabrel.authorization.models.entities.Group;
import com.tericcabrel.authorization.models.entities.User;
import com.tericcabrel.authorization.repositories.GroupRepository;
import com.tericcabrel.authorization.repositories.UserRepository;
import com.tericcabrel.authorization.services.interfaces.HierarchyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

        return groupRepository.save(newGroup);
    }

    @Override
    public User addUserToGroup(String email, String groupName) {


        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Group group = groupRepository.findByName(groupName).orElseThrow(() -> new IllegalArgumentException("Group not found"));

        List<String> pathsToAdd = Arrays.asList(group.getPath().split("/")).stream()
                .filter(segment -> !segment.isEmpty())
                .reduce(new ArrayList<>(), (acc, segment) -> {
                    String previousPath = acc.isEmpty() ? "/" : acc.get(acc.size() - 1);
                    acc.add(previousPath + segment + "/");
                    return acc;
                }, (list1, list2) -> { list1.addAll(list2); return list1; });

        user.getGroupPaths().addAll(pathsToAdd);
        return userRepository.save(user);
    }

    @Override
    public List<User> findUsersInSubtree(String groupId) {

        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));
        String pathRegex = "^" + group.getPath();
        return userRepository.findByGroupPathRegex(pathRegex);
    }

    @Override
    public List<User> findSiblings(String email, String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        Query query = new Query();
        query.addCriteria(Criteria.where("email").ne(email));
        query.addCriteria(Criteria.where("groupPaths").in(group.getPath()));
        return mongodb.find(query, User.class);
    }

    @Override
    public List<User> findChildern(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        List<String> userGroupPaths = user.getGroupPaths();
        if (userGroupPaths == null || userGroupPaths.isEmpty()) {
            return List.of();
        }
        Set<String> childPathRegexes = userGroupPaths.stream()
                .map(path -> "^" + path + "[^/]+/$") // Regex for direct children
                .collect(Collectors.toSet());
        Query groupQuery = new Query();
        groupQuery.addCriteria(new Criteria().orOperator(
                childPathRegexes.stream()
                        .map(regex -> Criteria.where("path").regex(regex))
                        .collect(Collectors.toList())
        ));
        List<Group> immediateChildGroups = mongodb.find(groupQuery, Group.class);

        if (immediateChildGroups.isEmpty()) {
            return List.of();
        }
        List<String> immediateChildGroupPaths = immediateChildGroups.stream()
                .map(Group::getPath)
                .collect(Collectors.toList());

        Query userQuery = new Query();
        userQuery.addCriteria(Criteria.where("groupPaths").in(immediateChildGroupPaths));

        List<User> childUsers = mongodb.find(userQuery, User.class);
        return childUsers.stream().distinct().collect(Collectors.toList());
    }
}
