package com.acme.users.mgt.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.dto.port.users.v1.UserDto;
import com.acme.users.mgt.dto.port.users.v1.UsersDisplayListDto;
import com.acme.users.mgt.services.api.users.IUserPortService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UsersController {
    private final IUserPortService userPortService;

    @PostMapping(value = "/api/v1/tenants/{tenantUid}/organizations/{orgUid}/users")
    public ResponseEntity<Object> createUser(@PathVariable("tenantUid") String tenantUid,
            @PathVariable(value = "orgUid") String orgUid,
            @RequestBody UserDto userDto) throws FunctionalException {
        UidDto uidDto = userPortService.createUser(tenantUid, orgUid, userDto);
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }

    @PostMapping(value = "/api/v1/tenants/{tenantUid}/organizations/{orgUid}/users/{userUid}")
    public ResponseEntity<Object> updateUser(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid, @PathVariable("userUid") String userUid,
            @RequestBody UserDto userDto) throws FunctionalException {
        userPortService.updateUser(tenantUid, orgUid, userUid, userDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/tenants/{tenantUid}/organizations/{orgUid}/users")
    public ResponseEntity<Object> listUsers(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid) throws FunctionalException {
        UsersDisplayListDto users = userPortService.findUsers(tenantUid, orgUid);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/v1/tenants/{tenantUid}/organizations/{orgUid}/users/{userUid}")
    public ResponseEntity<Object> deleteUser(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid, @PathVariable("userUid") String userUid) throws FunctionalException {
        userPortService.deleteUser(tenantUid, orgUid, userUid);
        return ResponseEntity.noContent().build();
    }

}