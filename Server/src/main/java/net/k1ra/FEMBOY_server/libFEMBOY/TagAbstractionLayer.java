package net.k1ra.FEMBOY_server.libFEMBOY;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import net.k1ra.FEMBOY_server.Utils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TagAbstractionLayer {

    static Predictor<Image, Classifications> predictor = null;

    public static void init() {
        try {
            DDTranslator translator = new DDTranslator();
            translator.load_tags();

            File model_file = new File(Utils.get_local_storage_dir()+ "DD-model.zip");

            Criteria<Image, Classifications> criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Classifications.class)
                            .optModelUrls(model_file.toURL().toString())
                            .optTranslator(translator)
                            .build();
            ZooModel model = ModelZoo.loadModel(criteria);

            predictor = model.newPredictor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tag_image(File file, int id, float aspect) {
        try {
            List<String> tags = new ArrayList<String>();

            Image image = ImageFactory.getInstance().fromFile(Paths.get(file.toURI()));
            image.getWrappedImage();

            //init DeepDanbooru if not initialised
            if (predictor == null) {
                init();
                Utils.populate_chara_tags();
            }

            Classifications classifications = predictor.predict(image);

            for (Classifications.Classification item : classifications.items()) {
                if (item.getProbability() > 0.5) {
                    tags.add(item.getClassName());
                }
            }


            DatabaseAbstractionLayer.upload_image(id, tags, aspect);
            DatabaseAbstractionLayer.update_global_tag_list(tags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
