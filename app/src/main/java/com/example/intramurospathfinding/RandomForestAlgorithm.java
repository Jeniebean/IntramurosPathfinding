package com.example.intramurospathfinding;

/*
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Evaluation;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.Random;

public class RandomForestAlgorithm {

    public static void main(String[] args) throws Exception {
        // Load dataset
        DataSource source = new DataSource("C:\\Users\\Winmri\\AndroidStudioProjects\\IntramurosPathfinding\\app\\sampledata\\tourist_navigation.arff");
        Instances dataset = source.getDataSet();

// Convert string attributes to word vectors
        StringToWordVector filter = new StringToWordVector();
        filter.setInputFormat(dataset);
        dataset = Filter.useFilter(dataset, filter);

        // Set class index (which attribute will be predicted, here we'll predict path_type)
        dataset.setClassIndex(dataset.numAttributes() - 3); // Assuming 'path_type' is the target attribute

        // Initialize Random Forest model
        RandomForest rf = new RandomForest();
        rf.setNumIterations(100); // Number of trees in the forest

        // Train Random Forest
        rf.buildClassifier(dataset);

        // Evaluate the model
        Evaluation eval = new Evaluation(dataset);
        eval.crossValidateModel(rf, dataset, 5, new Random(1));

        // Print the evaluation summary
        System.out.println(eval.toSummaryString("\nResults\n======\n", false));


        // Save the model (optional)
        SerializationHelper.write("C:\\Users\\Winmri\\AndroidStudioProjects\\IntramurosPathfinding\\app\\src\\main\\java\\com\\example\\intramurospathfinding\\models\\randomForestModel.model", rf);
        System.out.println("Model saved successfully!");
    }
}
*/

