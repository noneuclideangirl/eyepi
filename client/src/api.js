function apiRequest(args) {
    console.log(args);
    return fetch('http://localhost:9017', {
          method: 'post',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify(args),
        })
    .then(results => results.json());
}

function requestListServices(callback, err=() => {}) {
    apiRequest({
              command: 'LIST_SERVICES'
          })
    .then(data => {
        if (data.status === true) {
            callback(data.services);
        } else {
            err();
        }
    })
    .catch(console.log);
}

function requestStopService(serviceName) {
    return apiRequest({
        command: 'STOP_SERVICE',
        name: serviceName,
    });
}

function requestStartService(serviceName) {
    return apiRequest({
        command: 'START_SERVICE',
        name: serviceName,
    });
}

function requestServiceStartTime(serviceName) {
    return apiRequest({
        command: 'SERVICE_START_TIME',
        name: serviceName,
    });
}

function requestServiceLogs(serviceName) {
    return apiRequest({
        command: 'SERVICE_LOGS',
        name: serviceName,
    });
}

function requestDeleteService(serviceName) {
    return apiRequest({
        command: 'DELETE_SERVICE',
        name: serviceName
    });
}

function requestEditService(name, params) {
    const command = { command: 'EDIT_SERVICE' };
    return apiRequest({...command, ...params});
}

export {
    requestListServices,
    requestStopService,
    requestStartService,
    requestServiceStartTime,
    requestServiceLogs,
    requestDeleteService,
    requestEditService
};