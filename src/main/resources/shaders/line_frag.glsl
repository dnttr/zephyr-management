#version 330 core

out vec4 FragColor;

in float alpha_channel;

uniform vec3 line_color;

void main()
{
    FragColor = vec4(line_color, alpha_channel);
}