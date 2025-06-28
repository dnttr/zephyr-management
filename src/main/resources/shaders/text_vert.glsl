#version 330 core

layout (location = 0) in vec2 r_text_position;
layout (location = 1) in vec2 r_tex_coords;
layout(location = 2) in float r_character_index;

out vec2 tex_coords;

flat out int character_index;
flat out int characters_amount;

uniform int total_characters_amount;

uniform float begin_text_scale;
uniform float end_text_scale;
uniform float speed_text_scale;

uniform mat4 projection_matrix;

uniform float time;

///todo: reveal animation
void main()
{
    character_index = int(r_character_index);
    characters_amount = total_characters_amount;

    float scale = min(end_text_scale, begin_text_scale + time * speed_text_scale);

    gl_Position = projection_matrix * vec4(r_text_position * scale, 0.0, 1.0);
    tex_coords = r_tex_coords;
}
