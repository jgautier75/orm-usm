package com.acme.users.mgt.dto.port.users.v1;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class UsersDisplayListDto {
    private List<UserDisplayDto> users;
}
