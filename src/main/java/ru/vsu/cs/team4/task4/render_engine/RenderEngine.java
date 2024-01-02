package ru.vsu.cs.team4.task4.render_engine;

import ru.vsu.cs.team4.task4.Affine.affineComposite.Scale;
import ru.vsu.cs.team4.task4.LoadedModel;
import ru.vsu.cs.team4.task4.Scene;
import ru.vsu.cs.team4.task4.math.Point2f;
import ru.vsu.cs.team4.task4.math.matrix.Matrix4f;
import ru.vsu.cs.team4.task4.math.vector.Vector2f;
import ru.vsu.cs.team4.task4.math.vector.Vector3f;
import ru.vsu.cs.team4.task4.model.ModelTriangulated;
import ru.vsu.cs.team4.task4.model.Polygon;
import ru.vsu.cs.team4.task4.rasterization.ZBufferPixelWriter;
import ru.vsu.cs.team4.task4.rasterization.Rasterization;

import java.util.Arrays;
import java.util.Scanner;

public class RenderEngine {
    private static void renderModel(final int[] buffer, int width, int height,
                                    final Matrix4f modelViewProjectionMatrix,
                                    final ModelTriangulated mesh, int[][] textureARGB, float[][] Z) {

        for (Polygon polygon : mesh.getPolygons()) {
            Vector3f v1 = GraphicConveyor.multiplyMVPMatrixByVertex(modelViewProjectionMatrix, mesh.getVertices().get(polygon.getVertexIndices().get(0)));
            Vector3f v2 = GraphicConveyor.multiplyMVPMatrixByVertex(modelViewProjectionMatrix, mesh.getVertices().get(polygon.getVertexIndices().get(1)));
            Vector3f v3 = GraphicConveyor.multiplyMVPMatrixByVertex(modelViewProjectionMatrix, mesh.getVertices().get(polygon.getVertexIndices().get(2)));

            Point2f p1 = GraphicConveyor.vertexToPoint(v1, width, height);
            Point2f p2 = GraphicConveyor.vertexToPoint(v2, width, height);
            Point2f p3 = GraphicConveyor.vertexToPoint(v3, width, height);

            Vector2f vt1 = mesh.getTextureVertices().get(polygon.getTextureVertexIndices().get(0));
            Vector2f vt2 = mesh.getTextureVertices().get(polygon.getTextureVertexIndices().get(1));
            Vector2f vt3 = mesh.getTextureVertices().get(polygon.getTextureVertexIndices().get(2));

            ZBufferPixelWriter pixelWriter = (x, y, z, color) -> {
                System.out.println("rasterizing at " + x + " " + y);
                if(x < width && y < height && x > 0 && y > 0){
                    if(/*z < Z[x][y]*/true){
                        Z[x][y] = z;
                        buffer[width + height * y] = color;
                    }
                }
            };

            Rasterization.fillTriangle(pixelWriter, p1.getX(), p1.getY(), v1.getZ(), p2.getX(), p2.getY(), v2.getZ(), p3.getX(), p3.getY(), v3.getZ(),
                    vt1.getX(), vt1.getY(), vt2.getX(), vt2.getY(), vt3.getX(), vt3.getY(), textureARGB);
        }
    }


    public static void renderScene(final int[] buffer, int width, int height,
                                   final Camera camera,
                                   final Scene scene) throws Exception {
        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate(new Vector3f(1, 1, 1), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
        Matrix4f viewMatrix = GraphicConveyor.lookAt(camera.getPosition(), camera.getTarget());
        Matrix4f projectionMatrix = GraphicConveyor.perspective(camera.getFov(), camera.getAspectRatio(), camera.getNearPlane(), camera.getFarPlane());
        Matrix4f modelViewProjectionMatrix = new Matrix4f(projectionMatrix.getValues());
        modelViewProjectionMatrix.mulMut(viewMatrix);
        modelViewProjectionMatrix.mulMut(modelMatrix);

        float[][] Z = new float[width][height];
        for (float[] row : Z) {
            Arrays.fill(row, Float.POSITIVE_INFINITY);
        }

        for (LoadedModel loadedModel : scene.getModels()) {
            if (loadedModel.isActive()) {
                renderModel(buffer, width, height, modelViewProjectionMatrix, loadedModel.getModel(), loadedModel.getTextureARGB(), Z);
            }
        }
    }

}
