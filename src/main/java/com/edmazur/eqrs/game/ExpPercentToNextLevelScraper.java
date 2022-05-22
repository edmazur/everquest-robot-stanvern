package com.edmazur.eqrs.game;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;

// TODO: Clean this class up. It's largely copy/pasted from some scratch code I'd thrown together
// awhile ago, so it's not very clean. Alternatively, as long as the unit tests continue to pass
// with a wide range of sample input, maybe it's fine to just leave this as a black box that you
// don't need to look at much. However, if you find yourself needing to tweak this a lot as you see
// more types of screenshots, then it's probably worth refactoring.
public class ExpPercentToNextLevelScraper {

  // Debug tips:
  // 1. Set this flag to true.
  // 2. Run test against only the image in question.
  // 3. Open image in question in GIMP and get the row number of the top of "next level" (note that
  //    you need to mouseover quadrant 2 of a pixel to get its position). Set DEBUG_ROW to this.
  // 4. Save test output for row from step 3 to pom.txt.
  // 5. Replace "1354" with the second value of the first line of pom.txt:
  //    $ cat pom.txt | cut -d ',' -f 2 | xargs -I{} echo {} - 1354 | bc
  // 6. Compare output to NEXT_LEVEL_STRING_OFFSETS.
  private static final boolean DEBUG = false;
  private static final int DEBUG_ROW = 88;

  // Relative to the white pixel of the N, the offsets of the other white pixels
  // in the row. Multiple strategies are listed because blending happens on
  // in-game lighting.
  private static final List<HashSet<Integer>> NEXT_LEVEL_STRING_OFFSET_STRATEGIES = Arrays.asList(
      // Pure white.
      new HashSet<>(Arrays.asList(
          // The N.
          0, 6,
          // The first E.
          9, 10, 11, 12, 13, 14,
          // The T.
          26, 27, 28, 29, 30, 31, 32,
          // The first L.
          39,
          // The second E.
          46, 47, 48, 49, 50, 51,
          // The V.
          54, 61,
          // The third E.
          64, 65, 66, 67, 68, 69,
          // The last L.
          73)),
      new HashSet<>(Arrays.asList(
          0, 6, 9, 29, 39, 46, 54, 61, 64, 73)),
      new HashSet<>(Arrays.asList(
          0, 6, 9, 29, 39, 46, 64, 73)),
      new HashSet<>(Arrays.asList(
          0, 9, 10, 11, 12, 13, 21, 23, 24, 25, 26, 27, 28))
  );

  private static final List<Integer> WHITEISH_STRATEGIES =
      Arrays.asList(225, 230);

  // Relative to the white pixel of the N, the offsets of the middle row of the
  // exp bar.
  private static final int NEXT_LEVEL_EXP_BAR_OFFSET_X = -13;
  private static final int NEXT_LEVEL_EXP_BAR_OFFSET_Y = 15;

  public Optional<Integer> scrape(File image) {
    BufferedImage bufferedImage = null;
    try {
      bufferedImage = ImageIO.read(image);
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }

    // Get the pixels.
    int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
    PixelGrabber pixelGrabber = new PixelGrabber(
        bufferedImage,
        0,
        0,
        bufferedImage.getWidth(),
        bufferedImage.getHeight(),
        pixels,
        0,
        bufferedImage.getWidth());
    try {
      pixelGrabber.grabPixels();
    } catch (InterruptedException e) {
      System.err.println("Problem grabbing pixels from image");
      e.printStackTrace();
      return null;
    }

    // Organize the pixels.
    int[][] allPixels = new int[bufferedImage.getHeight()][];
    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      allPixels[y] = Arrays.copyOfRange(
          pixels, y * bufferedImage.getWidth(), (y + 1) * bufferedImage.getWidth() - 1);
    }

    // Process the pixels.
    for (int y = 0; y < allPixels.length; y++) {
      Integer exp = getExpFromImageRow(y, allPixels[y], allPixels);
      if (exp != null) {
        return Optional.of(exp);
      }
    }

    // If you get this far, you didn't find the exp bar.
    return Optional.empty();
  }

  private static Integer getExpFromImageRow(int y, int[] rowPixels, int[][] allPixels) {
    int nextLevelStringX = getNextLevelStringX(y, rowPixels);
    if (nextLevelStringX != -1) {
      if (DEBUG) {
        System.out.println("found next level at " + y + "," + nextLevelStringX);
      }
      int expBarMiddleX = nextLevelStringX  + NEXT_LEVEL_EXP_BAR_OFFSET_X;
      int expBarMiddleY = y + NEXT_LEVEL_EXP_BAR_OFFSET_Y;
      int[] expBarPixels = getExpBarPixels(expBarMiddleX, expBarMiddleY, allPixels);
      return getExpFromBar(expBarPixels);
    } else {
      return null;
    }
  }

  // If the row is the top row of the "NEXT LEVEL" string, then this returns the
  // x offset of the top left of the "N".
  // Otherwise, returns -1.
  private static int getNextLevelStringX(int y, int[] rowPixels) {
    for (HashSet<Integer> nextLevelStringOffsets : NEXT_LEVEL_STRING_OFFSET_STRATEGIES) {
      for (int whiteishThreshold : WHITEISH_STRATEGIES) {
        int nextLevelStringMaxOffset = Collections.max(nextLevelStringOffsets);
        outer: for (int x = 0; x < rowPixels.length; x++) {
          // Avoid checking past the image size.
          if (x + nextLevelStringMaxOffset >= rowPixels.length) {
            continue;
          }

          Color color = getColorFromPixel(rowPixels[x]);
          if (isWhiteish(color, whiteishThreshold)) {
            if (DEBUG && DEBUG_ROW == y) {
              System.out.println("found whiteish at " + y + "," + x);
            }
            for (int i = 1; i <= nextLevelStringMaxOffset; i++) {
              Color pixelToCheckColor = getColorFromPixel(rowPixels[x + i]);
              if (DEBUG && DEBUG_ROW == y) {
                System.out.println("checking pixel " + (x + i));
              }
              boolean expectingWhite = nextLevelStringOffsets.contains(i);
              if (expectingWhite) {
                if (!isWhiteish(pixelToCheckColor, whiteishThreshold)) {
                  continue outer;
                }
              } else {
                if (isWhiteish(pixelToCheckColor, whiteishThreshold)) {
                  continue outer;
                }
              }
            }
            return x;
          }
        }
      }
    }
    return -1;
  }

  private static boolean isWhiteish(Color color, int whiteishThreshold) {
    return color.getRed() >= whiteishThreshold
        && color.getGreen() >= whiteishThreshold
        && color.getBlue() >= whiteishThreshold;
  }

  private static Color getColorFromPixel(int pixel) {
    int alpha = (pixel >> 24) & 0xff;
    int red   = (pixel >> 16) & 0xff;
    int green = (pixel >>  8) & 0xff;
    int blue  = (pixel) & 0xff;
    return new Color(red, green, blue, alpha);
  }

  private static int[] getExpBarPixels(int expBarMiddleX, int expBarMiddleY, int[][] allPixels) {
    return Arrays.copyOfRange(allPixels[expBarMiddleY], expBarMiddleX, expBarMiddleX + 101);
  }

  private static Integer getExpFromBar(int[] pixels) {
    for (int exp = 0; exp < pixels.length; exp++) {
      Color color = getColorFromPixel(pixels[exp]);
      if (color.getBlue() != 0) {
        return exp - 1;
      }
    }

    System.err.println("Error getting exp from exp bar");
    return null;
  }

}
