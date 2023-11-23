package com.acme.jga.users.mgt.dto.ids;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class CompositeId {
	private Long id;
	private String uid;
}
