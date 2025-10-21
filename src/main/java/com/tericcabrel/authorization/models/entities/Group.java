package com.tericcabrel.authorization.models.entities;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Accessors(chain = true)
@Data
@Document(collection = "groups")
public class Group extends BaseModel {

    @Indexed
    protected String name;

    protected String slug;

    @Indexed
    protected String path;

    protected String parentId;
}
