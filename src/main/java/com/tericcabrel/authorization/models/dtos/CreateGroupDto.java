package com.tericcabrel.authorization.models.dtos;

import com.tericcabrel.authorization.models.entities.Group;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class CreateGroupDto {

    @NotBlank(message = "The name is required")
    private String name;

    private String parentName;

    public Group toGroup() {
        return new Group()
                .setName(this.name)
                .setParentId(this.parentName); // todo
    }
}
