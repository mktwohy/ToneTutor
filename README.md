# ToneTutor
[Watch Demo](https://www.youtube.com/watch?v=Vf4jq-M7OHs)

## Overview
When beginner musicians are learning how to play an instrument, it’s relatively easy to determine if you’ve played the right notes or not. However, they often overlook the timbre of these notes, which is equally important. Timbre is the quality of a note; for example, warm and round vs sharp and bright

Furthermore, the reason why instruments sound different (even when playing the same note) is the way that a particular instrument exaggerates the overtones of the note. Additionally, different playing techniques - such as picking vs plucking, or playing near the bridge vs near the neck - effect the timbre because they excite the harmonics of the string in different ways.

**This project aims to invent the means for quantifying this aspect of music, so that beginner musicians can receive realtime, visual feedback to train their ear and improve their playing technique.**

ToneTutor assigns a number to timbre with three steps:
1. it uses pitch detection to determine the fundamental note
2. it uses Fourier transformation to figure out the overtones corresponding to that note. 
    - In ToneTutor, this is called the harmonic fingerprint. 
3. it calculates a weighted sum of the harmonic fingerprint, with higher overtones having a higher weight. 
    - In ToneTutor, this is called a benya. 
      - In general, a smaller benya results from a warm note, whereas a large benya results from a bright note. This can be seen in the middle. 

## Source Code Shortcuts
- [ToneTutor src](https://github.com/mktwohy/ToneTutor/tree/master/app/src/main/java/com/example/tonetuner_v2)
- [SignalLib](https://github.com/mktwohy/Synth/tree/Main/SignalLib/src/main/java/com/example/signallib)
(from my [Synth Repository](https://github.com/mktwohy/Synth))
