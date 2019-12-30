import React from 'react';
import { requestListServices, requestStopService, requestStartService, requestServiceStartTime, requestServiceLogs } from './api';
import { TimeCounter } from './util';

class Service extends React.Component {
    constructor(props) {
      super(props);

      this.refreshUptime();
    }

    state = {
      startTime: undefined,
      showingLog: false,
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

    render() {
      let startStopService;
      if (this.props.active) {
        startStopService = (<button onClick={() => this.props.stopService(this.props.name)}>Stop</button>);
      } else {
        startStopService = (<button onClick={() => this.props.startService(this.props.name)}>Start</button>);
      }

      return (
        <div>
          <div>
            <span className="service-name">{this.props.name}</span>
            <span className="status">
              <span className="status-element">{this.props.active ? "Running" : "Stopped"}</span>
              {this.state.startTime !== undefined && <TimeCounter startTime={this.state.startTime} />}
            </span>
          </div>
          <div>
            {this.props.description}
          </div>
          <div>
            {startStopService}
            <button onClick={() => this.showLogs()}>Show logs</button>
            {this.state.showingLog && (<button onClick={() => this.hideLogs()}>Hide</button>)}
          </div>
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
          description: service.desc
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
                        />
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
