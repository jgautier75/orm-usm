package com.acme.users.mgt.events;

import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

import com.acme.users.mgt.logging.services.api.ILogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventBusErrorHandler implements ErrorHandler {
    private final ILogService logService;

    @Override
    public void handleError(Throwable t) {
        logService.errorS(this.getClass().getName(), t.getMessage(), null);
    }

}
