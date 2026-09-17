#version 150
#moj_import <fog.glsl>
in vec3 Position;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 PatchOrigin;
uniform sampler2D HeightField;
uniform float TickDelta;
uniform int FogShape;
out vec2 localXZ;
out vec3 waterNormal;
out float vertexDistance;
float h(ivec2 p) {
    vec4 encoded = texelFetch(HeightField, clamp(p, ivec2(0), ivec2(96)), 0);
    float current = dot(encoded.rg, vec2(255.0, 65280.0)) / 65535.0 * 4.0 - 2.0;
    float previous = dot(encoded.ba, vec2(255.0, 65280.0)) / 65535.0 * 4.0 - 2.0;
    return mix(previous, current, TickDelta);
}
void main() {
    localXZ = Position.xz;
    ivec2 p = ivec2(round(localXZ * 2.0));
    float center = h(p), left = h(p + ivec2(-1, 0)), right = h(p + ivec2(1, 0));
    float back = h(p + ivec2(0, -1)), front = h(p + ivec2(0, 1));
    waterNormal = normalize(vec3(left - right, 1.0, back - front));
    // FluidRenderer lowers source-water top vertices by 0.001 blocks.
    vec3 relativePosition = Position + PatchOrigin + vec3(0.0, center - 0.001, 0.0);
    gl_Position = ProjMat * ModelViewMat * vec4(relativePosition, 1.0);
    vertexDistance = fog_distance(relativePosition, FogShape);
}
