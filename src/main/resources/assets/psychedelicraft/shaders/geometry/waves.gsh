#version 150

in vec4 Position;

uniform float worldTicks;
uniform int worldTime;
uniform vec3 playerPos;
uniform float bigWaves;
uniform float smallWaves;
uniform float wiggleWaves;
uniform float distantWorldDeformation;
uniform vec4 fractal0TexCoords;
uniform float surfaceFractal;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

out vec2 texFractal0Coords;

void main() {
  gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

  if (surfaceFractal > 0.0) {
    texFractal0Coords = vec2(
        mix(fractal0TexCoords[0], fractal0TexCoords[2], (mod(Position.x + Position.y, 4.0)) / 4.0),
        mix(fractal0TexCoords[1], fractal0TexCoords[3], (mod(Position.z + Position.y, 4.0)) / 4.0)
    );
  }

  vec3 vVertex = gl_Position;
  //gl_FogFragCoord = length(vVertex);

  if (smallWaves > 0.0) {
    float w1 = 8.0;

    gl_Position[1] += sin((Position.x + worldTicks / 5.0) / w1 * 3.14159 * 2.0) * sin((Position.z + worldTicks / 5.0) / w1 * 3.14159 * 2.0) * smallWaves * 1.5;
    gl_Position[1] -= sin((playerPos.x + worldTicks / 5.0) / w1 * 3.14159 * 2.0) * sin((playerPos.z + worldTicks / 5.0) / w1 * 3.14159 * 2.0) * smallWaves * 1.5;

    float w2 = 16.0;

    gl_Position.y += sin((Position.x + worldTicks / 8.0) / w2 * 3.14159 * 2.0) * sin((Position.z) / w2 * 3.14159 * 2.0) * smallWaves * 3.0;
    gl_Position.y -= sin((playerPos.x + worldTicks / 8.0) / w2 * 3.14159 * 2.0) * sin((playerPos.z) / w2 * 3.14159 * 2.0) * smallWaves * 3.0;

    gl_Position.x = mix(gl_Position.x, gl_Position[0] * (1.0 + gl_FogFragCoord / 20.0), smallWaves);
    gl_Position.y = mix(gl_Position.y, gl_Position[1] * (1.0 + gl_FogFragCoord / 20.0), smallWaves);
  }

  if (wiggleWaves > 0.0) {
    float w1 = 8.0;
    gl_Position.x += sin((Position.y + worldTicks / 8.0) / w1 * 3.14159 * 2.0)
                    * sin((Position.z + worldTicks / 5.0) / w1 * 3.14159 * 2.0)
                    * wiggleWaves;
  }

  if (distantWorldDeformation > 0.0 && gl_FogFragCoord > 5.0) {
    gl_Position.y += (sin(gl_FogFragCoord / 8.0 * 3.14159 * 2.0) + 1.0)
                     * distantWorldDeformation
                     * (gl_FogFragCoord - 5.0) / 8.0;
  }

  if (bigWaves > 0.0) {
    if (gl_Position[2] > 0.1) {
      float dDist = (gl_Position[2] - 0.1) * bigWaves;
      if (gl_Position[2] > 20.0) {
        dDist = (20.0 - 0.1) * bigWaves + (gl_Position.z - 20.0) * bigWaves * 0.3;
      }

      float inf1 = sin(worldTicks * 0.0086465563) * dDist;
      float inf2 = cos(worldTicks * 0.0086465563) * dDist;
      float inf3 = sin(worldTicks * 0.0091033941) * dDist;
      float inf4 = cos(worldTicks * 0.0091033941) * dDist;
      float inf5 = sin(worldTicks * 0.0064566190) * dDist;
      float inf6 = cos(worldTicks * 0.0064566190) * dDist;

      float pMul = 1.3;

      gl_Position.x += sin(gl_Position.z * 0.1 * sin(worldTicks * 0.001849328) + worldTicks * 0.014123412) * 0.5 * inf1 * pMul;
      gl_Position.y += cos(gl_Position.z * 0.1 * sin(worldTicks * 0.001234728) + worldTicks * 0.017481893) * 0.4 * inf1 * pMul;

      gl_Position.x += sin(gl_Position.y * 0.1 * sin(worldTicks * 0.001523784) + worldTicks * 0.021823911) * 0.2 * inf2 * pMul;
      gl_Position.y += sin(gl_Position.x * 0.1 * sin(worldTicks * 0.001472387) + worldTicks * 0.023193141) * 0.08 * inf2 * pMul;

      gl_Position.x += sin(gl_Position.z * 0.15 * sin(worldTicks * 0.001284923) + worldTicks * 0.019404289) * 0.25 * inf3 * pMul;
      gl_Position.y += cos(gl_Position.z * 0.15 * sin(worldTicks * 0.001482938) + worldTicks * 0.018491238) * 0.15 * inf3 * pMul;

      gl_Position.x += sin(gl_Position.y * 0.05 * sin(worldTicks * 0.001283942) + worldTicks * 0.012942342) * 0.4 * inf4 * pMul;
      gl_Position.y += sin(gl_Position.x * 0.05 * sin(worldTicks * 0.001829482) + worldTicks * 0.012981328) * 0.35 * inf4 * pMul;

      gl_Position.z += sin(gl_Position.y * 0.13 * sin(worldTicks * 0.02834472) + worldTicks * 0.023482934) * 0.1 * inf5 * pMul;
      gl_Position.z += sin(gl_Position.x * 0.124 * sin(worldTicks * 0.00184298) + worldTicks * 0.018394082) * 0.05 * inf6 * pMul;
      gl_Position.w += sin(gl_Position.y * 0.13 * sin(worldTicks * 0.02834472) + worldTicks * 0.023482934) * 0.1 * inf5 * pMul;
      gl_Position.w += sin(gl_Position.x * 0.124 * sin(worldTicks * 0.00184298) + worldTicks * 0.018394082) * 0.05 * inf6 * pMul;
    }
  }
}