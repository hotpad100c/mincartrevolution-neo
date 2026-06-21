package ml.mypals.minecartrevolution.client.sound;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;
import org.jspecify.annotations.NonNull;

public class DirectBufferAudioStream implements AudioStream {
  private final ByteBuffer pcmData;
  private final AudioFormat format;

  public DirectBufferAudioStream(ByteBuffer pcmData, int sampleRate, int channels) {
    this.pcmData = pcmData.duplicate();
    this.format = new AudioFormat(sampleRate, 16, channels, true, false);
  }

  @Override
  public @NonNull AudioFormat getFormat() {
    return this.format;
  }

  @Override
  public @NonNull ByteBuffer read(int size) {
    int toRead = Math.min(size, this.pcmData.remaining());
    ByteBuffer result = this.pcmData.slice().order(this.pcmData.order());
    result.limit(toRead);
    this.pcmData.position(this.pcmData.position() + toRead);
    return result.order(ByteOrder.LITTLE_ENDIAN);
  }

  @Override
  public void close() {}
}
