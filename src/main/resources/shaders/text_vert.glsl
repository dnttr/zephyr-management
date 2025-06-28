#version 330 core

layout (location = 0) in vec2 position;
layout (location = 1) in vec2 texCoord;
layout(location = 2) in float characterIndex;

flat out int charIndex;
out vec2 TexCoords;

uniform float time;
uniform float maxScale;
uniform float startingScale;
uniform float speedScaling;
uniform mat4 projection;

void main()
{
    float charOffset = float(charIndex) * 0.2f;
    float scale = min(maxScale, startingScale + time * speedScaling);
    gl_Position = projection * vec4(position * scale, 0.0, 1.0);
    charIndex = int(characterIndex);
    TexCoords = texCoord;
}
