/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.maze;

import net.minecraft.util.WeightedRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Created by lukas on 20.06.14.
 */
public class MazeGeneratorWithComponents
{
    public static List<MazeComponentPosition> generatePaths(Random rand, Maze maze, List<MazeComponent> mazeComponents)
    {
        List<MazeComponentPosition> positions = new ArrayList<>();

        Stack<MazeRoom> positionStack = new Stack<>();

        // Gather needed start points
        for (MazePath path : maze.allPaths())
        {
            if (maze.get(path) == Maze.ROOM)
            {
                positionStack.push(path.getSourceRoom());
                positionStack.push(path.getDestinationRoom());
            }
        }

        ArrayList<MazeComponentPosition> validComponents = new ArrayList<>();

        while (!positionStack.empty())
        {
            MazeRoom position = positionStack.pop();
            validComponents.clear();

            for (MazeComponent component : mazeComponents)
            {
                for (MazeRoom attachedRoom : component.getRooms())
                {
                    MazeRoom componentPosition = position.sub(attachedRoom);

                    if (canComponentBePlaced(maze, new MazeComponentPosition(component, componentPosition)))
                    {
                        validComponents.add(new MazeComponentPosition(component, componentPosition));
                    }
                }
            }

            if (validComponents.size() == 0)
            {
                System.out.println("Did not find fitting component for maze!");

                continue;
            }

            boolean allZero = true;
            for (MazeComponentPosition component : validComponents)
            {
                if (component.getComponent().itemWeight > 0)
                {
                    allZero = false;
                    break;
                }
            }

            MazeComponentPosition generatingComponent;
            if (allZero)
            {
                generatingComponent = validComponents.get(rand.nextInt(validComponents.size()));
            }
            else
            {
                generatingComponent = (MazeComponentPosition) WeightedRandom.getRandomItem(rand, validComponents);
            }

            for (MazeRoom room : generatingComponent.getComponent().getRooms())
            {
                MazeRoom roomInMaze = generatingComponent.getPositionInMaze().add(room);
                maze.set(Maze.ROOM, roomInMaze);

                MazePath[] neighbors = Maze.getNeighborPaths(maze.dimensions.length, roomInMaze);
                for (MazePath neighbor : neighbors)
                {
                    if (maze.get(neighbor) == Maze.NULL)
                    {
                        maze.set(Maze.WALL, neighbor);
                    }
                }
            }

            for (MazePath exit : generatingComponent.getComponent().getExitPaths())
            {
                MazePath exitInMaze = exit.add(generatingComponent.getPositionInMaze());
                MazeRoom destRoom = exitInMaze.getDestinationRoom();
                MazeRoom srcRoom = exitInMaze.getSourceRoom();

                if (maze.get(destRoom) == Maze.NULL)
                {
                    positionStack.push(destRoom);
                }

                if (maze.get(srcRoom) == Maze.NULL)
                {
                    positionStack.push(srcRoom);
                }

                maze.set(Maze.ROOM, exitInMaze);
            }

            positions.add(generatingComponent);
        }

        return positions;
    }

    public static boolean canComponentBePlaced(Maze maze, MazeComponentPosition component)
    {
        for (MazeRoom room : component.getComponent().getRooms())
        {
            MazeRoom roomInMaze = room.add(component.getPositionInMaze());
            byte curValue = maze.get(roomInMaze);

            if (curValue != Maze.NULL)
            {
                return false;
            }

            MazePath[] roomNeighborPaths = Maze.getNeighborPaths(maze.dimensions.length, roomInMaze);
            for (MazePath roomNeighborPath : roomNeighborPaths)
            {
                byte neighborValue = maze.get(roomNeighborPath);
                if (neighborValue == Maze.ROOM && !component.getComponent().getExitPaths().contains(roomNeighborPath.sub(component.getPositionInMaze())))
                {
                    return false;
                }
            }
        }

        for (MazePath exit : component.getComponent().getExitPaths())
        {
            byte curValue = maze.get(exit.add(component.getPositionInMaze()));

            if (curValue != Maze.ROOM && curValue != Maze.NULL && curValue != Maze.INVALID)
            {
                return false;
            }
        }

        return true;
    }
}
