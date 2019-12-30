import React from 'react';

class TimeCounter extends React.Component {
    state = { time: new Date().getTime() };

    render() {
        const uptime = (this.state.time - this.props.startTime) / 1000;
        const seconds = Math.round(uptime % 60);
        const minutes = Math.round((uptime / 60) % 60);
        const hours = Math.round(uptime / 3600);
        return (
        <span className="status-element">{hours}:{minutes.toString().padStart(2, '0')}:{seconds.toString().padStart(2, '0')}</span>
        );
    }

    componentDidMount() {
        this.interval = setInterval(() => this.setState({ time: new Date().getTime() }), 1000);
    }

    componentWillUnmount() {
        clearInterval(this.interval);
    }
}

export { TimeCounter };