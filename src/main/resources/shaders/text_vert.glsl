#version 330 core

layout (location = 0) in vec2 position;
layout (location = 1) in vec2 texCoord;
layout(location = 2) in float characterIndex;

flat out int charIndex;
flat out float revealAlpha;
out vec2 TexCoords;

uniform float time;
uniform float maxScale;
uniform float startingScale;
uniform float speedScaling;
uniform mat4 projection;

uniform float revealSpeed;
uniform int revealDirection;
uniform int totalChars;

void main()
{//todo: reveal animation
    charIndex = int(characterIndex);
    float charOffset = float(charIndex) * 0.2f;
    float scale = min(maxScale, startingScale + time * speedScaling);
    vec2 finalPosition = position;

    gl_Position = projection * vec4(finalPosition * scale, 0.0, 1.0);
    TexCoords = texCoord;
}
