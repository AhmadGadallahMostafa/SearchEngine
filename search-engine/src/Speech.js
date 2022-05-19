import React from 'react';
import { useState } from 'react';
import SpeechRecognition, { useSpeechRecognition } from 'react-speech-recognition';
import error from './error.png';
import './Speech.css';
import micIconDis from './micDis.png';
import micIconAct from './micAct.png';

function Speech({handleSpeech}) {
    /*initializing the speech recognition*/
    const [isListening, setIsListening] = useState(false);
    const [isActive, setIsActive] = useState("mic-dis");
    const [micIcon, setMicIcon] = useState(micIconDis);
    const { transcript } = useSpeechRecognition();
    const isSupported = SpeechRecognition.browserSupportsSpeechRecognition();

    const handleClick = () => {
        setIsListening(!isListening);
        if (isListening) {
            setIsActive("mic-dis");
            setMicIcon(micIconDis);
            SpeechRecognition.stopListening();
            handleSpeech(transcript);
        }
        else {
            setIsActive("mic-act");
            setMicIcon(micIconAct);
            SpeechRecognition.startListening({ continuous: false, interimResults: true });
        }
    };

    return (
        < div className="speech-container" >
            {!isSupported ? (
                <div className="speech-container">
                    <img src={error} alt="error" className="error-img" />
                </div>
            ) : (
                <div>
                    <div className="speech-area">
                        <img src={micIcon} alt="microphone" className={isActive} id="mic-icon" onClick={handleClick} />
                    </div>
                </div>
            )
            }
        </div >
    );
}

export default Speech;