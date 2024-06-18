import java.awt.*;

public class Tetrominoe {
    //3d array for storing the different pieces in coordinate form
    public static final int[][][] pieces = { //{X, Y}
            {{0, 0}, {1, 0}, {2, 0}, {3, 0}}, //Line Piece (0)
            {{0, 0}, {0, 1}, {1, 1}, {2, 1}}, //J Piece (1)
            {{2, 0}, {2, 1}, {1, 1}, {0, 1}}, //L Piece (2)
            {{0, 0}, {0, 1}, {1, 0}, {1, 1}}, //Square Piece (3)
            {{1, 0}, {2, 0}, {0, 1}, {1, 1}}, //S Piece (4)
            {{1, 0}, {0, 1}, {1, 1}, {2, 1}}, //T Piece (5)
            {{0, 0}, {1, 0}, {1, 1}, {2, 1}} //Z Piece (6)
    };

    //Enum to store shape types
    enum Shape {
        Line,
        J,
        L,
        Square,
        S,
        T,
        Z
    }

    //X and Y translations
    public int x = 0;
    public int y = 0;

    public int[][] blocks; //Store the blocks used in the piece

    Shape shape; //Store the type of shape

    Color color; //Store the piece color

    public Tetrominoe(int selection) {
        //Selects a piece based on selection param
        blocks = pieces[selection];

        //Switch case to assigning color and shape type based on selection
        switch (selection) {
            case 0:
                shape = Shape.Line;
                color = Color.BLUE;
                break;
            case 1:
                shape = Shape.J;
                color = Color.CYAN;
                break;
            case 2:
                shape = Shape.L;
                color = Color.ORANGE;
                break;
            case 3:
                shape = Shape.Square;
                color = Color.YELLOW;
                break;
            case 4:
                shape = Shape.S;
                color = Color.RED;
                break;
            case 5:
                shape = Shape.T;
                color = Color.MAGENTA;
                break;
            case 6:
                shape = Shape.Z;
                color = Color.GREEN;
                break;
        }
    }

    //Function to rotate piece
    public void rotate(Boolean clockwise) {
        //No rotation to be applied for square shape
        if(shape != Shape.Square) {
            //Store original blocks
            int[][] originalBlocks = blocks.clone();

            //Create array for storing rotated piece
            int[][] rotatedPiece = new int[originalBlocks.length][originalBlocks[0].length];

            //Get the center block in the piece for setting origin in translation
            int[] centroid = shape != Shape.S ? originalBlocks[originalBlocks.length/2].clone() : originalBlocks[(originalBlocks.length/2) + 1].clone();

            //Select the proper rotation matrix based on clockwise boolean
            int[][] matrix = clockwise ? new int[][]{{0, 1}, {-1, 0}} : new int[][]{{0, -1}, {1, 0}};

            for (int i = 0; i < originalBlocks.length; i++) {
                //Translate the block to make the origin of the rotation the center of the piece
                int[] translatedBlock = new int[]{originalBlocks[i][0] - centroid[0] ,originalBlocks[i][1] - centroid[1]};

                //Apply the rotation matrix
                int[] rotatedBlock = multiplyMatrix(translatedBlock, matrix);

                //revers the translation so piece remains in same position
                rotatedBlock[0] += centroid[0];
                rotatedBlock[1] += centroid[1];

                //Store rotated block
                rotatedPiece[i] = rotatedBlock;
            }

            //Set the piece to the new rotation
            blocks = rotatedPiece;
        }
    }

    //Function for multiplying 2 matrices together
    public int[] multiplyMatrix(int[] block, int[][] matrix) {
        int[] result = new int[block.length];
        for(int i = 0; i < block.length; i++) {
            for(int j = 0; j < block.length; j++) {
                //Multiply each element in the row of the block matrix by each element in the column of the rotation matrix
                result[i] += block[j] * matrix[j][i];
            }
        }

        //Return the resultant matrix
        return result;
    }
}