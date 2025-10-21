package com.tericcabrel.authorization.services.interfaces;

import com.tericcabrel.authorization.models.entities.Group;
import com.tericcabrel.authorization.models.entities.User;

import java.util.List;

public interface HierarchyService {

    /**
     * Creates a new group with a Materialized Path.
     * @param groupName The name of the new group.
     * @param parentName The name of the parent group, or null for a root group.
     * @return The newly created Group.
     */
    Group createGroup(String groupName, String parentName);

    /**
     * Adds a user to a specific group, updating their group paths with all ancestors.
     * @param email The email-ID of the user.
     * @param groupName The name of the group.
     * @return The updated User object.
     */
    User addUserToGroup(String email, String groupName);

    /**
     * Finds all users belonging to a group or any of its subgroups.
     * @param groupId The ID of the group.
     * @return A list of users in the specified subtree.
     */
    List<User> findUsersInSubtree(String groupId);

    /**
     * Finds all users sibling to the user.
     * @param email The emailID of the user.
     * @param groupId The id of the group.
     * @return A list of users in the specified subtree.
     */
    List<User> findSiblings(String email, String groupId);

    /**
     * Finds all immediate child users to the user.
     * @param email The emailID of the group.
     * @return A list of users in the specified subtree.
     */
    List<User> findChildern(String email);
}
