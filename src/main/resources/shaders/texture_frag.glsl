#version 330 core

out vec4 FragColor;

in vec2 coords;

uniform sampler2D m_texture;

void main() {
    FragColor = texture(m_texture, coords);
}
