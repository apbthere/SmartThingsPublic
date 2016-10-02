/**
 *  MythTVController
 *
 *  Copyright 2016 apbthere
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "MythTVController",
    namespace: "apbthere",
    author: "apbthere",
    description: "Enter MythTV Live TV mode",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("MythTV") {
		input name: "ipAddress", type: "text", title: "IP Address", required: true, multiple: false
        input name: "port", type: "number", title: "port", required: true, multiple: false
        input name: "channel", type: "number", title: "Channel", required: true, multiple: false
	}
    section("Enter Live TV on..") {
		input name: "startTime", title: "Turn On Time?", type: "time"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	schedule(startTime, "startTimerCallback")
}

def startTimerCallback() {
	log.debug "Entering Live TV"
    
    def command = new physicalgraph.device.HubAction(
    method: "GET",
    path: "/control/${settings.ipAddress}/6546",
    headers: [
        HOST: "${settings.ipAddress}:${settings.port}"
    ],
    query: [command: "play channel ${settings.channel}"]
	)
    
    sendHubCommand(command)

}