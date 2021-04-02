package net.k1ra.FEMBOY_server.libFEMBOY;

import ai.djl.modality.*;
import ai.djl.modality.cv.*;
import ai.djl.modality.cv.util.*;
import ai.djl.ndarray.*;
import ai.djl.translate.*;
import net.k1ra.FEMBOY_server.Utils;

import java.io.File;
import java.util.*;

class DDTranslator implements Translator<Image, Classifications> {

    private static List<String> CLASSES = new ArrayList<String>();

    public void load_tags() {
        try {
            Scanner s = new Scanner(new File(Utils.get_local_storage_dir()+ "DD-tags.txt"));
            while (s.hasNext())
                CLASSES.add(s.next());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDManager manager = ctx.getNDManager();
        NDArray array = input.toNDArray(manager, Image.Flag.COLOR);
        array = NDImageUtils.resize(array, 512).div(255.0f);
        return new NDList(array);
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list) {
        NDArray probabilities = list.singletonOrThrow();
        return new Classifications(CLASSES, probabilities);
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }
}
