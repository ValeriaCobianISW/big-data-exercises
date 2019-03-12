package nearsoft.academy.bigdata.recommendation;

import com.google.common.collect.HashBiMap;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    public static final int indexProduct = 19;
    public static final int indexUser = 15;
    public static final int indexScore = 14;
    public static final String productRoute = "product/productId: ";
    public static final String userRoute = "review/userId: ";
    public static final String scoreRoute = "review/score: ";
    public static final String dataFile = "dataFile.txt";


    private int totalReviews;
    private HashMap<String, Integer> usersTotal;
    private HashBiMap<String, Integer> productsTotal;
    private Recommender recommender;

    /**
     * This method helps to create a recommender with the file data
     *
     * @param path is the data.zip.gz
     */
    public MovieRecommender(String path) {
        this.productsTotal = HashBiMap.create();
        this.usersTotal = new HashMap<String, Integer>();
        File file = setValues(path);
        createRecommender(file);
    }

    /**
     * This method helps to set values of our path to an external file.
     *
     * @param filePath is the field path.
     * @return File with format information.
     */
    private File setValues(String filePath) {
        try {
            BufferedReader br = readGzip(filePath);
            FileWriter writer = new FileWriter(dataFile);

            String readLn;
            String writeLn = "";
            int productSelected = 0;

            while ((readLn = br.readLine()) != null) {

                if (readLn.startsWith(productRoute)) {
                    String idProduct = readLn.substring(indexProduct);
                    if (!this.productsTotal.containsKey(idProduct)) {
                        this.productsTotal.put(idProduct, this.productsTotal.size());
                        productSelected = this.productsTotal.get(idProduct);
                    }
                } else if (readLn.startsWith(userRoute)) {
                    String idUser = readLn.substring(indexUser);

                    if (!this.usersTotal.containsKey(idUser)) {
                        this.usersTotal.put(idUser, usersTotal.size());
                    }
                    this.totalReviews++;

                    writeLn = this.usersTotal.get(idUser) + "," + productSelected + ",";
                } else if (readLn.startsWith(scoreRoute)) {
                    double score = Double.parseDouble(readLn.substring(indexScore));
                    writeLn += score + "\n";
                    writer.write(writeLn);
                    writer.flush();
                }
            }

            br.close();
            writer.close();

        } catch (IOException ex) {
            System.out.println(ex.toString());
        }

        return new File(dataFile);
    }

    /**
     * This method helps to  create a recommender.
     *
     * @param dataFile is the field with information.
     */
    public void createRecommender(File dataFile) {
        try {
            DataModel model = new FileDataModel(dataFile);
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /***
     *This method helps to get a total Reviews.
     * @return totalReviews of data file.
     */
    public int getTotalReviews() {
        return totalReviews;
    }

    /***
     * This method helps to get total products.
     * @return total of products of data file.
     */
    public int getTotalProducts() {
        return productsTotal.size();
    }

    /**
     * This method helps to get total of users.
     *
     * @return size of total users.
     */
    public int getTotalUsers() {
        return usersTotal.size();
    }

    /**
     * This method helps to create recommendations for user.
     *
     * @param id The user id to search.
     * @return A list of recommendations for user.
     */
    public List<String> getRecommendationsForUser(String id) {
        try {
            return getRecommendations(this.recommender.recommend(this.usersTotal.get(id), 3));
        } catch (TasteException ex) {
            System.out.println(ex.toString());
        }

        return null;
    }

    /**
     * This method helps to get recommendations.
     *
     * @param recommendations A list of recommendations.
     * @return A list of recomendation products.
     */
    private List<String> getRecommendations(List<RecommendedItem> recommendations) {
        ArrayList<String> products = new ArrayList<String>();

        for (RecommendedItem r : recommendations) {
            products.add(this.productsTotal.inverse().get((int) r.getItemID()));
        }

        return products;
    }

    /**
     * This method  helps to read a file GZip.
     *
     * @param path Path of the file.
     * @return A buffered reader with decoder.
     * @throws IOException
     */
    private static BufferedReader readGzip(String path) throws IOException {
        InputStream fileStream = new FileInputStream(path);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        return new BufferedReader(decoder);
    }
}
