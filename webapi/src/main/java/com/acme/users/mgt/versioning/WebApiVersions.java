package com.acme.users.mgt.versioning;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebApiVersions {
    public static final String V1 = "v1";
    public static final String API_PREFIX = "/api";
    public static final String V1_PREFIX = "/"+V1;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class TenantsResourceVersion {
        public static final String ROOT = API_PREFIX + V1_PREFIX + "/tenants";
        public static final String WITH_UID = API_PREFIX + V1_PREFIX + "/tenants/{uid}";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class OrganizationsResourceVersion {
        public static final String ROOT = API_PREFIX + V1_PREFIX + "/tenants/{tenantUid}/organizations";
        public static final String WITH_UID = API_PREFIX + V1_PREFIX + "/tenants/{tenantUid}/organizations/{orgUid}";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class UsersResourceVersion {
        public static final String ROOT = API_PREFIX + V1_PREFIX + "/tenants/{tenantUid}/organizations/{orgUid}/users";
        public static final String WITH_UID = API_PREFIX + V1_PREFIX
                + "/tenants/{tenantUid}/organizations/{orgUid}/users/{userUid}";
    }

}
