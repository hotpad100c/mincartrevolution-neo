package ml.mypals.minecartrevolution.manager;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;

/**
 * This is a class to simplify when finding annotation targets. It can simply just create a new
 * instance and run "find()" to find annotation targets. It can help you simplify some registry
 * things, so that you don't need to manually register.
 *
 * @author Mulatram
 */
public class AnnotationManager {
  private final Class<? extends Annotation> annotation;
  private final ElementType target;

  public AnnotationManager(Class<? extends Annotation> annotation, ElementType target) {
    this.annotation = annotation;
    this.target = target;
  }

  public List<ModFileScanData.AnnotationData> find() {
    List<ModFileScanData> scanData = ModList.get().getAllScanData();
    List<ModFileScanData.AnnotationData> ret = new ArrayList<>();
    scanData.forEach(scan -> scan.getAnnotatedBy(annotation, target).forEach(ret::add));
    return ret;
  }
}
