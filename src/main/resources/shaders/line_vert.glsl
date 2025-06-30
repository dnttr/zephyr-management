#version 330 core

layout (location = 0) in vec2 r_quad_position;
layout (location = 1) in vec2 r_instance_start_position;
layout (location = 2) in vec2 r_instance_end_position;
layout (location = 3) in float r_instance_alpha;

uniform mat4 projection_matrix;
uniform float line_width_pixels;
uniform float ball_radius;

out float alpha_channel;

void main()
{
    alpha_channel = r_instance_alpha;

    vec2 start_pos = r_instance_start_position;
    vec2 end_pos = r_instance_end_position;

    vec2 line_vec = end_pos - start_pos;
    float line_length = length(line_vec);

    if (line_length < 0.01)
    {
        gl_Position = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    vec2 line_dir = normalize(line_vec);
    vec2 perpendicular_dir = vec2(-line_dir.y, line_dir.x);

    float local_x = r_quad_position.x * line_length;
    float local_y = (r_quad_position.y - 0.5) * line_width_pixels;

    vec2 final_position = start_pos + (line_dir * local_x) + (perpendicular_dir * local_y);

    final_position.x += ball_radius / 2;
    final_position.y += ball_radius / 2;

    gl_Position = projection_matrix * vec4(final_position, 0.0, 1.0);
}