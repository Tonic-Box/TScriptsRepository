package net.runelite.client.plugins.tscripts.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Predicate;
import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
/**
 * Various Image/BufferedImage utilities.
 */
public class ImageUtil
{
    static
    {
        ImageIO.setUseCache(false);
    }

    /**
     * Creates a {@link BufferedImage} from an {@link Image}.
     *
     * @param image An Image to be converted to a BufferedImage.
     * @return      A BufferedImage instance of the same given image.
     */
    public static BufferedImage bufferedImageFromImage(final Image image)
    {
        if (image instanceof BufferedImage)
        {
            return (BufferedImage) image;
        }

        return toARGB(image);
    }

    /**
     * Creates an ARGB {@link BufferedImage} from an {@link Image}.
     */
    public static BufferedImage toARGB(final Image image)
    {
        if (image instanceof BufferedImage && ((BufferedImage) image).getType() == BufferedImage.TYPE_INT_ARGB)
        {
            return (BufferedImage) image;
        }

        BufferedImage out = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = out.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return out;
    }

    /**
     * Offsets an image's luminance by a given value.
     *
     * @param rawImg  The image to be darkened or brightened.
     * @param offset A signed 8-bit integer value to brighten or darken the image with.
     *               Values above 0 will brighten, and values below 0 will darken.
     * @return       The given image with its brightness adjusted by the given offset.
     */
    public static BufferedImage luminanceOffset(final Image rawImg, final int offset)
    {
        BufferedImage image = toARGB(rawImg);
        final int numComponents = image.getColorModel().getNumComponents();
        final float[] scales = new float[numComponents];
        final float[] offsets = new float[numComponents];

        Arrays.fill(scales, 1f);
        for (int i = 0; i < numComponents; i++)
        {
            offsets[i] = offset;
        }
        // Set alpha to not offset
        offsets[numComponents - 1] = 0f;

        return offset(image, scales, offsets);
    }

    /**
     * Changes an images luminance by a scaling factor
     *
     * @param rawImg      The image to be darkened or brightened.
     * @param percentage The ratio to darken or brighten the given image.
     *                   Values above 1 will brighten, and values below 1 will darken.
     * @return           The given image with its brightness scaled by the given percentage.
     */
    public static BufferedImage luminanceScale(final Image rawImg, final float percentage)
    {
        BufferedImage image = toARGB(rawImg);
        final int numComponents = image.getColorModel().getNumComponents();
        final float[] scales = new float[numComponents];
        final float[] offsets = new float[numComponents];

        Arrays.fill(offsets, 0f);
        for (int i = 0; i < numComponents; i++)
        {
            scales[i] = percentage;
        }
        // Set alpha to not scale
        scales[numComponents - 1] = 1f;

        return offset(image, scales, offsets);
    }

    /**
     * Offsets an image's alpha component by a given offset.
     *
     * @param rawImg  The image to be made more or less transparent.
     * @param offset A signed 8-bit integer value to modify the image's alpha component with.
     *               Values above 0 will increase transparency, and values below 0 will decrease
     *               transparency.
     * @return       The given image with its alpha component adjusted by the given offset.
     */
    public static BufferedImage alphaOffset(final Image rawImg, final int offset)
    {
        BufferedImage image = toARGB(rawImg);
        final int numComponents = image.getColorModel().getNumComponents();
        final float[] scales = new float[numComponents];
        final float[] offsets = new float[numComponents];

        Arrays.fill(scales, 1f);
        Arrays.fill(offsets, 0f);
        offsets[numComponents - 1] = offset;
        return offset(image, scales, offsets);
    }

    /**
     * Offsets an image's alpha component by a given percentage.
     *
     * @param rawImg      The image to be made more or less transparent.
     * @param percentage The ratio to modify the image's alpha component with.
     *                   Values above 1 will increase transparency, and values below 1 will decrease
     *                   transparency.
     * @return           The given image with its alpha component scaled by the given percentage.
     */
    public static BufferedImage alphaOffset(final Image rawImg, final float percentage)
    {
        BufferedImage image = toARGB(rawImg);
        final int numComponents = image.getColorModel().getNumComponents();
        final float[] scales = new float[numComponents];
        final float[] offsets = new float[numComponents];

        Arrays.fill(scales, 1f);
        Arrays.fill(offsets, 0f);
        scales[numComponents - 1] = percentage;
        return offset(image, scales, offsets);
    }

    /**
     * Creates a grayscale image from the given image.
     *
     * @param image The source image to be converted.
     * @return      A copy of the given imnage, with colors converted to grayscale.
     */
    public static BufferedImage grayscaleImage(final BufferedImage image)
    {
        final Image grayImage = GrayFilter.createDisabledImage(image);
        return ImageUtil.bufferedImageFromImage(grayImage);
    }

    /**
     * Re-size a BufferedImage to the given dimensions.
     *
     * @param image the BufferedImage.
     * @param newWidth The width to set the BufferedImage to.
     * @param newHeight The height to set the BufferedImage to.
     * @return The BufferedImage with the specified dimensions
     */
    public static BufferedImage resizeImage(final BufferedImage image, final int newWidth, final int newHeight)
    {
        return resizeImage(image, newWidth, newHeight, false);
    }

    /**
     * Re-size a BufferedImage to the given dimensions.
     *
     * @param image the BufferedImage.
     * @param newWidth The width to set the BufferedImage to.
     * @param newHeight The height to set the BufferedImage to.
     * @param preserveAspectRatio Whether to preserve the original image's aspect ratio. When {@code true}, the image
     *                               will be scaled to have a maximum of {@code newWidth} width and {@code newHeight}
     *                               height.
     * @return The BufferedImage with the specified dimensions
     */
    public static BufferedImage resizeImage(final BufferedImage image, final int newWidth, final int newHeight, final boolean preserveAspectRatio)
    {
        final Image resized;
        if (preserveAspectRatio)
        {
            if (image.getWidth() > image.getHeight())
            {
                resized = image.getScaledInstance(newWidth, -1, Image.SCALE_SMOOTH);
            }
            else
            {
                resized = image.getScaledInstance(-1, newHeight, Image.SCALE_SMOOTH);
            }
        }
        else
        {
            resized = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        }
        return ImageUtil.bufferedImageFromImage(resized);
    }

    /**
     * Re-size a BufferedImage's canvas to the given dimensions.
     *
     * @param image     The image whose canvas should be re-sized.
     * @param newWidth  The width to set the BufferedImage to.
     * @param newHeight The height to set the BufferedImage to.
     * @return          The BufferedImage centered within canvas of given dimensions.
     */
    public static BufferedImage resizeCanvas(final BufferedImage image, final int newWidth, final int newHeight)
    {
        final BufferedImage dimg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        final int centeredX = newWidth / 2 - image.getWidth() / 2;
        final int centeredY = newHeight / 2 - image.getHeight() / 2;

        final Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(image, centeredX, centeredY, null);
        g2d.dispose();
        return dimg;
    }

    /**
     * Rotates an image around its center by a given number of radians.
     *
     * @param image The image to be rotated.
     * @param theta The number of radians to rotate the image.
     * @return      The given image, rotated by the given theta.
     */
    public static BufferedImage rotateImage(final BufferedImage image, final double theta)
    {
        AffineTransform transform = new AffineTransform();
        transform.rotate(theta, image.getWidth() / 2.0, image.getHeight() / 2.0);
        AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        return transformOp.filter(image, null);
    }

    /**
     * Flips an image horizontally and/or vertically.
     *
     * @param image      The image to be flipped.
     * @param horizontal Whether the image should be flipped horizontally.
     * @param vertical   Whether the image should be flipped vertically.
     * @return           The given image, flipped horizontally and/or vertically.
     */
    public static BufferedImage flipImage(final BufferedImage image, final boolean horizontal, final boolean vertical)
    {
        int x = 0;
        int y = 0;
        int w = image.getWidth();
        int h = image.getHeight();

        final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = out.createGraphics();

        if (horizontal)
        {
            x = w;
            w *= -1;
        }

        if (vertical)
        {
            y = h;
            h *= -1;
        }

        g2d.drawImage(image, x, y, w, h, null);
        g2d.dispose();

        return out;
    }

    /**
     * Outlines non-transparent pixels of a BufferedImage with the given color.
     *
     * @param image The image to be outlined.
     * @param color The color to use for the outline.
     * @return      The BufferedImage with its edges outlined with the given color.
     */
    public static BufferedImage outlineImage(final BufferedImage image, final Color color)
    {
        return outlineImage(image, color, false);
    }

    /**
     * Outlines non-transparent pixels of a BufferedImage with the given color. Optionally outlines
     * corners in addition to edges.
     *
     * @param image          The image to be outlined.
     * @param color          The color to use for the outline.
     * @param outlineCorners Whether to draw an outline around corners, or only around edges.
     * @return               The BufferedImage with its edges--and optionally, corners--outlined
     *                       with the given color.
     */
    public static BufferedImage outlineImage(final BufferedImage image, final Color color, final Boolean outlineCorners)
    {
        final BufferedImage filledImage = fillImage(image, color);
        final BufferedImage outlinedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g2d = outlinedImage.createGraphics();
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                if ((x == 0 && y == 0)
                        || (!outlineCorners && Math.abs(x) + Math.abs(y) != 1))
                {
                    continue;
                }

                g2d.drawImage(filledImage, x, y, null);
            }
        }
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return outlinedImage;
    }

    /**
     * @see #loadImageResource(Class, String)
     */
    @Deprecated
    public static BufferedImage getResourceStreamFromClass(Class<?> c, String path)
    {
        return loadImageResource(c, path);
    }

    /**
     * Reads an image resource from a given path relative to a given class.
     * This method is primarily shorthand for the synchronization and error handling required for
     * loading image resources from the classpath.
     *
     * @param c    The class to be referenced for the package path.
     * @param path The path, relative to the given class.
     * @return     A {@link BufferedImage} of the loaded image resource from the given path.
     */
    public static BufferedImage loadImageResource(final Class<?> c, final String path)
    {
        try (InputStream in = c.getResourceAsStream(path))
        {
            synchronized (ImageIO.class)
            {
                return ImageIO.read(in);
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(path, e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(path, e);
        }
    }

    /**
     * Fills all non-transparent pixels of the given image with the given color.
     *
     * @param image The image which should have its non-transparent pixels filled.
     * @param color The color with which to fill pixels.
     * @return      The given image with all non-transparent pixels set to the given color.
     */
    public static BufferedImage fillImage(final BufferedImage image, final Color color)
    {
        final BufferedImage filledImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < filledImage.getWidth(); x++)
        {
            for (int y = 0; y < filledImage.getHeight(); y++)
            {
                int pixel = image.getRGB(x, y);
                int a = pixel >>> 24;
                if (a == 0)
                {
                    continue;
                }

                filledImage.setRGB(x, y, color.getRGB());
            }
        }
        return filledImage;
    }

    /**
     * Performs a rescale operation on the image's color components.
     *
     * @param image   The image to be adjusted.
     * @param scales  An array of scale operations to be performed on the image's color components.
     * @param offsets An array of offset operations to be performed on the image's color components.
     * @return        The modified image after applying the given adjustments.
     */
    private static BufferedImage offset(final BufferedImage image, final float[] scales, final float[] offsets)
    {
        return new RescaleOp(scales, offsets, null).filter(image, null);
    }

    /**
     * Recolors pixels of the given image with the given color based on a given recolor condition
     * predicate.
     *
     * @param image            The image which should have its non-transparent pixels recolored.
     * @param color            The color with which to recolor pixels.
     * @param recolorCondition The condition on which to recolor pixels with the given color.
     * @return The given image with all pixels fulfilling the recolor condition predicate
     * set to the given color.
     */
    public static BufferedImage recolorImage(final BufferedImage image, final Color color, final Predicate<Color> recolorCondition)
    {
        final BufferedImage recoloredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < recoloredImage.getWidth(); x++)
        {
            for (int y = 0; y < recoloredImage.getHeight(); y++)
            {
                final Color pixelColor = new Color(image.getRGB(x, y), true);
                if (!recolorCondition.test(pixelColor))
                {
                    recoloredImage.setRGB(x, y, image.getRGB(x, y));
                    continue;
                }

                recoloredImage.setRGB(x, y, color.getRGB());
            }
        }
        return recoloredImage;
    }

    public static BufferedImage recolorImage(BufferedImage image, final Color color)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        WritableRaster raster = image.getRaster();

        for (int xx = 0; xx < width; xx++)
        {
            for (int yy = 0; yy < height; yy++)
            {
                int[] pixels = raster.getPixel(xx, yy, (int[]) null);
                pixels[0] = color.getRed();
                pixels[1] = color.getGreen();
                pixels[2] = color.getBlue();
                raster.setPixel(xx, yy, pixels);
            }
        }
        return image;
    }
}