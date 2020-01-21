import React from 'react';
import {
  requestListServices,
  requestStopService,
  requestStartService,
  requestServiceStartTime,
  requestServiceLogs,
  requestDeleteService,
  requestEditService
} from './api';
import { TimeCounter } from './util';

class Service extends React.Component {
    constructor(props) {
      super(props);

      this.refreshUptime();
    }

    state = {
      startTime: undefined,
      showingLog: false,
      editing: false,
    };
    logboxId = this.props.name + "-logbox";

    // Refresh the uptime counter. If this service is active, request the uptime; otherwise, hide the timer component.
    refreshUptime() {
      if (this.props.active) {
        requestServiceStartTime(this.props.name)
          .then(result => {
            if (result.status) {
              const timestamp = parseInt(result.data.$numberLong);
              this.setState({ startTime: timestamp});
            }
          });
      } else {
        this.setState({ startTime: undefined });
      }
    }

    componentDidUpdate(prevProps) {
      if (prevProps.active !== this.props.active) {
        this.refreshUptime();
      }
    }

    hideLogs() {
      this.setState({ showingLog: false });
      document.getElementById(this.logboxId).style.display = 'none';
    }

    showLogs() {
      requestServiceLogs(this.props.name).then(result => {
        if (result.status) {
          this.setState({ showingLog: true });
          let logbox = document.getElementById(this.logboxId);
          // replace newlines with the appropriate HTML entity
          logbox.innerHTML = result.data.replace(/\n/g, '&#13;&#10;');
          logbox.style.display = 'block';
        }
      });
    }

    saveChanges() {
      const newName = document.getElementById("edit-service-name").innerText;
      const newDesc = document.getElementById("edit-service-desc").innerText;

      let params = {};
      if (newName !== this.props.name) {
        params.name = newName;
      }
      if (newDesc !== this.props.description) {
        params.desc = newDesc;
      }

      this.props.editService(this.props.name, params, () => this.setState({ editing: false }));
    }

    render() {
      let startStopService;
      if (this.props.active) {
        startStopService = (<button onClick={() => this.props.stopService(this.props.name)}>Stop</button>);
      } else {
        startStopService = (<button onClick={() => this.props.startService(this.props.name)}>Start</button>);
      }

      let controls, title, description;
      if (this.state.editing) {
        controls = (
          <div>
            <button onClick={() => this.saveChanges()}>Save</button>
          </div>
        );
        title = (
          <div>
          <span id="edit-service-name" className="service-name" contentEditable>{this.props.name}</span>
            <span className="status">
              <span className="status-element">{this.props.active ? "Running" : "Stopped"}</span>
              {this.state.startTime !== undefined && <TimeCounter startTime={this.state.startTime} />}
            </span>
          </div>
        );
        description = (
          <div id="edit-service-desc" contentEditable>
            {this.props.description}
          </div>
        );
      } else {
        controls = (
          <div>
            <button onClick={() => this.setState({ editing: true })}>Edit</button><br></br>
            {startStopService}
            {this.state.showingLog
              ? (<button onClick={() => this.hideLogs()}>Hide logs</button>)
              : (<button onClick={() => this.showLogs()}>Show logs</button>)}
          </div>
        );
        title = (
          <div>
            <span className="service-name">{this.props.name}</span>
            <span className="status">
              <span className="status-element">{this.props.active ? "Running" : "Stopped"}</span>
              {this.state.startTime !== undefined && <TimeCounter startTime={this.state.startTime} />}
            </span>
          </div>
        );
        description = (
          <div>
            {this.props.description}
          </div>
        );
      }

      return (
        <div>
          {title}
          {description}
          {controls}
          <textarea spellCheck="false" autoComplete="false" autoCorrect="false" readOnly id={this.logboxId} className="logbox"></textarea>
        </div>
      );
    }
}

// Loads and displays a list of all current services
class ServiceList extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      services: [],
      loaded: false,
      seq: 0,
    };

    this.loadServices();
  }

  loadServices() {
    console.log("Reloading services");
    requestListServices(services => {
      let serviceMap = {};
      services.forEach(service => {
        serviceMap[service.name] = {
          name: service.name,
          active: service.active,
          description: service.desc,
          id: service.id
        };
      });
      this.setState({
          services: serviceMap,
          loaded: true,
      });
    });
  }
  
  startService(name) {
    requestStartService(name).then(result => {
      if (result.status) {
        this.setServiceValue(name, 'active', true);
      }
    });
  }

  setServiceValue(name, key, value) {
    let newServiceMap = {};
    Object.assign(newServiceMap, this.state.services);
    newServiceMap[name][key] = value;
    this.setState({ services: newServiceMap });
  }

  stopService(name) {
    requestStopService(name).then(result => {
      if (result.status) {
        this.setServiceValue(name, 'active', false);
      }
    });
  }

  editService(name, params, callback) {
    params.id = this.state.services[name].id;
    requestEditService(name, params).then(result => {
      if (result.status) {
        this.loadServices();
        callback();
      }
    });
  }

  componentDidMount() {
    this.interval = setInterval(() => this.loadServices(), 60000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  render() {
    return (
      <div className="service-list">
        <header>
          <h2>Service list</h2>
          {
            this.state.loaded
              ? (<ul> {
                  Object.values(this.state.services).map(service => (
                      <li key={service.name}>
                        <Service
                          name={service.name}
                          description={service.description}
                          active={service.active}
                          stopService={name => this.stopService(name)}
                          startService={name => this.startService(name)}
                          editService={(name, params, callback) => this.editService(name, params, callback)}
                        />
                        <div>
                          <button onClick={() => {
                            if (window.confirm("Delete service " + service.name + "?")) {
                              requestDeleteService(service.name).then(result => {
                                if (result.status) {
                                  this.loadServices();
                                }
                              });
                            }
                          }}>Delete</button>
                        </div>
                      </li>
                    ))
                } </ul>)
              : (<span>Loading...</span>)
          }
        </header>
      </div>
    )
  }

}

function App() {
  return (
    <div>
      <ServiceList />
      <footer>by <a href="https://noneuclideangirl.net/">noneuclideangirl.net</a></footer>
    </div>
  )
}

export default App;
