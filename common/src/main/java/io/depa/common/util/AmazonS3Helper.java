package io.depa.common.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.depa.common.context.ApplicationContext;
import io.depa.common.exception.CustomException;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

public final class AmazonS3Helper {

    // Injections
    private static final Vertx vertx = ApplicationContext.getVertx();
    // Amazon Web Services
    private static final AmazonS3 amazonS3 = ApplicationContext.getAmazonS3();
    private static final JsonObject bucket = ApplicationContext.getConfig().getJsonObject("aws").getJsonObject("s3.bucket");

    public static Completable deleteObject(String url) {
        if (vertx != null && url != null) {
            String bucketName = bucket.getString("cdn");
            String key = url.substring(url.indexOf(bucketName) + bucketName.length() + 1);

            return vertx.rxExecuteBlocking(promise -> {
                try {
                    amazonS3.deleteObject(bucketName, key);
                    promise.complete();
                } catch (Exception e) {
                    promise.fail(e);
                }
            }).ignoreElement();
        } else {
            return Completable.complete();
        }
    }

    public static Maybe<String> getConfig(String key) {
        if (vertx != null && key != null) {
            String bucketName = bucket.getString("config");

            return vertx.rxExecuteBlocking(promise -> {
                try {
                    amazonS3.getObjectAsString(bucketName, key);
                    promise.complete(amazonS3.getObjectAsString(bucketName, key));
                } catch (Exception e) {
                    promise.fail(e);
                }
            });
        }
        return vertx == null ? Maybe.error(CustomException.VERTX_INJECTION_FAILED) : Maybe.empty();
    }

    public static Maybe<String> renameObject(String newFileName, String url) {
        if (vertx != null) {
            String bucketName = bucket.getString("cdn");
            String key = url.substring(url.indexOf(bucketName) + bucketName.length() + 1);
            String newKey = url.substring(0, url.lastIndexOf('/')) + newFileName +
                    url.substring(url.lastIndexOf('.'));

            return vertx.rxExecuteBlocking(promise -> {
                try {
                    amazonS3.copyObject(bucketName, key, bucketName, newKey);
                    amazonS3.deleteObject(bucketName, key);
                    promise.complete("https://" + bucketName + '/' + newKey);
                } catch (Exception e) {
                    promise.fail(e);
                }
            });
        }
        return Maybe.error(CustomException.VERTX_INJECTION_FAILED);
    }

    public static Maybe<String> uploadImage(String fileName, String imageUrl) {
        if (vertx != null && fileName != null && imageUrl != null && !imageUrl.equals(Constants.EMPTY)) {
            String base64Data = imageUrl.substring(imageUrl.indexOf(',') + 1);
            if (org.apache.commons.codec.binary.Base64.isBase64(base64Data)) {
                byte[] bytes = Base64.getDecoder().decode(base64Data);
                InputStream inputStream = new ByteArrayInputStream(bytes);

                String contentType = imageUrl.substring(imageUrl.indexOf(':') + 1, imageUrl.indexOf(';'));
                String fileFormat = contentType.substring(contentType.indexOf('/') + 1);

                String bucketName = bucket.getString("cdn");
                String key = "images/" + fileFormat + '/' + fileName + '.' + fileFormat;

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(bytes.length);
                metadata.setContentType("image/" + fileFormat);

                return vertx.rxExecuteBlocking(promise -> {
                    try {
                        amazonS3.putObject(bucketName, key, inputStream, metadata);
                        promise.complete("https://" + bucketName + '/' + key);
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                });
            }
        }
        return vertx == null ? Maybe.error(CustomException.VERTX_INJECTION_FAILED) : Maybe.empty();
    }
}
