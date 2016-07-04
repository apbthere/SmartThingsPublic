/**
 *  Fan Director
 *
 *  Copyright 2016 abthere
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
    name: "Fan Director",
    namespace: "abthere",
    author: "abthere",
    description: "Turn on bathroom fan when motion is detected or humidity is too high",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Bathroom motion sensor") {
		input "m", "capability.motionSensor", multiple: false
	}
    section("Bathroom humidity sensor") {
		input "h", "capability.relativeHumidityMeasurement", multiple: false, title: "Which humidity sensor?"
	}
    section("Bathroom fan") {
		input "s", "capability.switch", multiple: false
	}
    section("Turn fan on if humidity is greater then") {
		input "humidityLevel", "number"
	}
    section("Outside humidity sensor") {
		input "oh", "capability.relativeHumidityMeasurement", multiple: false, title: "Which humidity sensor?"
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
	subscribe(h, "humidity", humidityHandler)
    subscribe(m, "motion", motionHandler)
    subscribe(s, "switch", switchHandler)
}

def humidityHandler(shower) {
  def humidity = shower.value.toInteger() 
  if (humidity > humidityLevel) {
  	log.debug "Relative humidity is ${shower.value}. Checking the outside humidity."
    
    oh.poll()
    def outisedHumidity = oh.value.toInteger()
    log.debug( "Relative Humidity Outside: ${outisedHumidity}" )
    
    if (outsideHumidity > humidity) {
		log.debug "Outside is more humid than inside. Do not use fan."
    } else {
    	log.debug "Turning ON fan"
        s.on()
    }
  } else {
  	log.debug "Inside humidity is ${humidity}, truning off fan"
    s.off()
  }
}  

def switchHandler(evt) {
	log.debug "$evt.name: ${evt.value}"
    if("off" == evt.value) {
    	state.lastOff = now()
    } else {
//    	runIn(turnoffAfter, "scheduledTurnOff")
    }
}

def motionHandler(evt) {
	if("active" == evt.value && "on" != myswitch.currentSwitch) {
    	if(state.lastOff) {
	        def elapsed = now() - state.lastOff
    	    log.debug "Time since last off event is ${elapsed/1000}"
            s.on()
        } else {
        	s.on()
        }
    }
}    