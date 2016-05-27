/**
 *  Copyright 2015 SmartThings
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
 *  Big Turn OFF
 *
 *  Author: SmartThings
 */
definition(
    name: "Turn pool lights ON",
    namespace: "abthere",
    author: "abthere",
    description: "Turn pool lights on",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("Pool lights") {
		input "s", "capability.switch", multiple: false
	}
}

def installed()
{
	log.debug "Installed with settings: ${settings}"
	subscribe(location, "sunset", sunsetHandler)
}

def updated()
{
	unsubscribe()
	subscribe(location, "sunset", sunsetHandler)
}

def sunsetHandler(evt) {
	log.debug "Sun has set!"
    
    s.on()
    runIn(2, turnOff)
}

def turnOff() {
	s.off()
    runIn(2, turnOn)
}

def turnOn() {
	s.on()
}