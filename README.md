# Lissajous & Rose Oscilloscope Synth

This project is a small Java Swing application that generates Lissajous and rose-curve patterns and sends them to stereo audio output. The idea is to connect the output to an oscilloscope in XY mode so the waveform traces the shape on the screen.

## What it does

- Lissajous mode: generates two sine waves with independent frequencies and phase
- Rose-curve mode: generates a polar rose pattern with adjustable petal count and rotation
- Shared controls for trace rate and signal level
- Auto-rotation / auto-phase sweep for each mode
- Visual oscilloscope-style display in the app window

## Requirements

- Java JDK 8 or newer
- JSyn library in the project `lib` folder

The project expects a JSyn JAR inside `lib/`, for example:

- `lib/jsyn.jar`

## Project layout

- `src/` — Java source files
- `lib/` — external JSyn dependency
- `build.txt` — Bash build script
- `run.txt` — Bash runner script
- `README.md` — usage notes

## Build

From the project root:

```bash
bash build.txt
```

This creates an `out/` directory and compiles the Java sources.

## Run

From the project root:

```bash
bash run.txt
```

On Windows CMD, the equivalent is:

```cmd
mkdir out
javac -cp lib\jsyn.jar -sourcepath src -d out src\SynthEngine.java src\LissajousSynth.java src\RoseSynth.java src\LissajousPanel.java src\RosePanel.java src\OscilloscopeApp.java
java -cp "out;lib\jsyn.jar" OscilloscopeApp
```

## Usage notes

- Start the synthesizer with the Play button.
- Use the mode selector to switch between Lissajous and Rose curves.
- Adjust the frequency / petal count and phase values to change the displayed pattern.
- For a real oscilloscope, connect the left channel to X and the right channel to Y, then switch the oscilloscope to XY mode.
- If there is no audio device available, the program may not play sound correctly.

## Troubleshooting

- If you see `no jsyn jar found in lib/`, make sure the JSyn JAR is present in `lib/`.
- If the app fails to start, confirm that Java is installed and the classpath matches the JAR location.
- If the screen is blank or the controls do not respond, rebuild the project before running it again.
