/**
 *  Bathroom director
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
    name: "Bathroom director",
    namespace: "abthere",
    author: "abthere",
    description: "Turn the light on when motion is detected and it's too dark",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Bathroom lights") {
		input "s", "capability.switch", multiple: false
	}
    
    section("Bathroom motion sensors") {
		input "m", "capability.motionSensor", multiple: false
	}
    
    section("Bathroom light sensors") {
		input "l", "capability.illuminanceMeasurement", multiple: false
	}
    
    section("Only turn on lights if lux is below...") {
		input "lux", "number"
	}
    
    section("Turn lights off if no motion is detected for number of seconds") {
		input "turnoffAfter", "number"
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
	subscribe(m, "motion", motionHandler)
    subscribe(s, "switch", switchHandler)
}

def switchHandler(evt) {
	log.debug "$evt.name: ${evt.value}"
    if("off" == evt.value) {
    	state.lastOff = now()
    } else {
    	runIn(turnoffAfter, "scheduledTurnOff")
    }
}

def motionHandler(evt) {
	if("active" == evt.value) {
    	unschedule("scheduledTurnOff")
        log.debug "Detected a motion, let's see if light is needed.." 
        def shouldCheck = true
        if(state.lastOff) {
	        def elapsed = now() - state.lastOff
    	    log.debug "Time since last off event is ${elapsed}"
            shouldCheck = elapsed > 5000
        }
        if(shouldCheck && l.latestValue("illuminance") <= lux) {
            log.debug "turning on the light because lux is ${l.latestValue("illuminance")}"
            s.on()
        }
	} else if("inactive" == evt.value) {
    	state.lastInactive = now()
        runIn(turnoffAfter, "scheduledTurnOff")
  	}
}

def scheduledTurnOff() {
	s.off()
	unschedule("scheduledTurnOff") // Temporary work-around to scheduling bug
}