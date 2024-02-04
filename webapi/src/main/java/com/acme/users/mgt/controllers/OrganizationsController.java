package com.acme.users.mgt.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationDto;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationListLightDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.services.api.organization.IOrganizationPortService;
import com.acme.users.mgt.versioning.WebApiVersions;
import com.acme.users.mgt.versioning.WebApiVersions.OrganizationsResourceVersion;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrganizationsController {
    private final IOrganizationPortService organizationPortService;
    private final SdkTracerProvider sdkTracerProvider;
    private final SdkMeterProvider sdkMeterProvider; 
    private final SdkLoggerProvider sdkLoggerProvider;

    @PostMapping(value = OrganizationsResourceVersion.ROOT)
    public ResponseEntity<UidDto> createOrganization(@PathVariable("tenantUid") String tenantUid,
            @RequestBody OrganizationDto organizationDto) throws FunctionalException {
        UidDto uidDto = organizationPortService.createOrganization(tenantUid, organizationDto);
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }

    @GetMapping(value = OrganizationsResourceVersion.ROOT)
    public ResponseEntity<OrganizationListLightDto> findOrgsByTenant(@PathVariable("tenantUid") String tenantUid)
            throws FunctionalException {
        Tracer tracer = sdkTracerProvider.get("ORGS", WebApiVersions.V1);
        Meter meter = sdkMeterProvider.get("ORGS-LIST");
        Logger otelLogger =  sdkLoggerProvider.get("ORGS-LIST-LOGS");
        Span span = tracer.spanBuilder("LIST").startSpan();
        OrganizationListLightDto lightList = null;
        try {
            otelLogger.logRecordBuilder().setSeverity(Severity.INFO).setBody("List organizations logs via opentelemetry").emit();
            ObservableDoubleMeasurement measurement =  meter.gaugeBuilder("orgs-list-gauge").buildObserver();
            long tStart = System.currentTimeMillis();
            lightList = organizationPortService.findAllOrgsLightByTenant(tenantUid);
            long tEnd = System.currentTimeMillis();
            measurement.record((tEnd-tStart));
        } catch (Exception t) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }

        return new ResponseEntity<>(lightList, HttpStatus.OK);
    }

    @GetMapping(value = OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<OrganizationDto> findOrgDetails(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid,
            @RequestParam(name = "fetchSectors", defaultValue = "false") boolean fecthSectors)
            throws FunctionalException {
        OrganizationDto orgDto = organizationPortService.findOrganizationByUid(tenantUid, orgUid, fecthSectors);
        return new ResponseEntity<>(orgDto, HttpStatus.OK);
    }

    @PostMapping(value = OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<Void> updateOrganization(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid, @RequestBody OrganizationDto organizationDto)
            throws FunctionalException {
        organizationPortService.updateOrganization(tenantUid, orgUid, organizationDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<Void> deleteOrganization(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid)
            throws FunctionalException {
        organizationPortService.deleteOrganization(tenantUid, orgUid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
