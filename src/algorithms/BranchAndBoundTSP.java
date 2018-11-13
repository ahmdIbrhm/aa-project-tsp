package algorithms;

import static algorithms.AbstractTSP.bestCircuit;
import static algorithms.AbstractTSP.minimumCost;
import java.util.ArrayList;
import java.util.List;
import model.Point;
import model.TSPInput;

public class BranchAndBoundTSP extends AbstractTSP {

    //what will be the output if its an adjancency matrix???
    int[][] adjacencyMatrix;
    List<Point> pointsToAnalyze;

    @Override
    public void execute(TSPInput input) {
        
        List<Point> points = input.getPoints();
        adjacencyMatrix = input.generate_adjancency_matrix();
        pointsToAnalyze = points;
        minimumCost = getFirstSolution(points);
        printMatrix(adjacencyMatrix);
        System.out.println("1st minimum: "+minimumCost);
        int[] array = new int[1];
        array[0] = 0;
        double predictedCost = getPredictedCost(array);
        TreeNodeInBranchAndBound root = new TreeNodeInBranchAndBound(0, predictedCost, array);
        branchAndBound(root);
    }

    public double[][] generateMatrix(List<Point> points) {
        double[][] adjancencyMatrix = new double[points.size()][points.size()];
        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < points.size(); j++) {
                adjancencyMatrix[i][j] = points.get(i).distanceTo(points.get(j));
            }
        }
        return adjancencyMatrix;
    }

    public double getFirstSolution(List<Point> points) {
        double cost = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            cost += points.get(i).distanceTo(points.get(i + 1));
        }
        cost += points.get(points.size() - 1).distanceTo(points.get(0));
        return cost;
    }

    public void branchAndBound(TreeNodeInBranchAndBound node) {
        //System.out.println(node.toString());
        if (node.getCostFunction() < minimumCost) 
        {
            TreeNodeInBranchAndBound child;
            //System.out.println("First minimum cost: "+minimumCost);
            if (node.getCities().length < adjacencyMatrix.length) {
                for (int i = 0; i < adjacencyMatrix.length; i++) 
                {
                    if (!(isInArray(node.getCities(), i))) 
                    {
                        double instantCost = node.getInstantCost() + adjacencyMatrix[node.getCities()[node.getCities().length - 1]][i];
                        int[] cities = new int[node.getCities().length + 1];
                        System.arraycopy(node.getCities(), 0, cities, 0, node.getCities().length);
                        cities[node.getCities().length] = i;
                        double predictedCost = getPredictedCost(cities);
                        double costFunction = instantCost + predictedCost;
                        child = new TreeNodeInBranchAndBound(instantCost, predictedCost, cities);
                        insertSorted(child, node.getChilds());
                        //to do: add child to the array list of childs and keep it sorted
                    }
                }
                for (int i = 0; i < node.getChilds().size(); i++) {
                    branchAndBound(node.getChilds().get(i));
                }
            } else if (node.getCities().length == adjacencyMatrix.length) //there is 1 step to reach the leaf
            {
                int[] nodeCities = node.getCities();
                double instantCost = node.getInstantCost() + adjacencyMatrix[nodeCities[nodeCities.length - 1]][nodeCities[0]];
                int[] cities = new int[nodeCities.length + 1];
                System.arraycopy(nodeCities, 0, cities, 0, nodeCities.length);
                cities[cities.length - 1] = nodeCities[0];  //put the last element as the first cause cycle
                double predictedCost = getPredictedCost(cities); //will be 0 since the cities are complete
                double costFunction = instantCost + predictedCost;
                child = new TreeNodeInBranchAndBound(instantCost, predictedCost, cities);
                branchAndBound(child);
            } 
            else //number of cities in the array is more than the number of cities (leaf of the tree)
            {
                if (node.getCostFunction() < minimumCost) 
                {
                    minimumCost = node.getCostFunction();
                    bestCircuit = sortPointsWithRespectToNode(node.getCities(), pointsToAnalyze);
                }
            }
        }
    }

    public void printArray(int array[]) 
    {
        for (int i = 0; i < array.length; i++) 
        {
            System.out.print("" + array[i] + " ");
        }
    }

    public boolean isInArray(int[] array, int number) 
    {
        for (int i = 0; i < array.length; i++) 
        {
            if (array[i] == number) 
            {
                return true;
            }
        }
        return false;
    }

    public void printMatrix(int[][] matrix) 
    {
        for (int i = 0; i < matrix.length; i++) 
        {
            for (int j = 0; j < matrix[0].length; j++) 
            {
                System.out.print(String.format("%.2f", matrix[i][j]) + "\t");
            }
            System.out.println();
        }
    }

    private double getPredictedCost(int[] cities) 
    {
        int[][] newMatrix = copyMatrix(adjacencyMatrix); //to modify the values freely
        double predictedCost = 0;
        if (cities.length > 1) 
        {
            for (int k = 0; k < cities.length; k++) 
            {
                if (k != cities.length - 1 || k == 0) 
                {
                    for (int i = 0; i < adjacencyMatrix.length; i++) 
                    {
                        newMatrix[cities[k]][i] = Integer.MAX_VALUE;  //delete row of i
                    }
                }
                if (k != 0) 
                {
                    for (int j = 0; j < adjacencyMatrix[0].length; j++) 
                    {
                        newMatrix[j][cities[k]] = Integer.MAX_VALUE;  //delete column of j
                    }
                }
            }
        }
        //System.out.println("\nNew Matrix: ");
        //printMatrix(newMatrix);
        for (int i = 0; i < newMatrix.length; i++) 
        {
            double minimumInARow = Double.MAX_VALUE;
            for (int j = 0; j < newMatrix[i].length; j++) 
            {
                if (newMatrix[i][j] < minimumInARow && i != j && newMatrix[i][j] != Double.MAX_VALUE) 
                {
                    minimumInARow = newMatrix[i][j];
                }
            }
            if (minimumInARow != Double.MAX_VALUE) 
            {
                predictedCost += minimumInARow;
            }
        }
        return predictedCost;
    }

    public int[][] copyMatrix(int[][] matrix) 
    {
        int[][] newMatrix = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                newMatrix[i][j] = matrix[i][j];
            }
        }
        return newMatrix;
    }

    void insertSorted(TreeNodeInBranchAndBound node, ArrayList<TreeNodeInBranchAndBound> list) 
    {
        int i = 0;
        for (i = 0; i < list.size(); i++) {
            if (list.get(i).getCostFunction() > node.getCostFunction()) {
                break;
            }
        }
        list.add(i, node);
    }

    private List<Point> sortPointsWithRespectToNode(int[] array, List<Point> pointsToAnalyze) 
    {
        List<Point> sortedPoints = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            sortedPoints.add(pointsToAnalyze.get(array[i]));
        }
        return sortedPoints;
    }

}
