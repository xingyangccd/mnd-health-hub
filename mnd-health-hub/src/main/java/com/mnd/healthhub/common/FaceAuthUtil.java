package com.mnd.healthhub.common;

import cn.hutool.core.util.StrUtil;
import com.mnd.healthhub.exception.HisException;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FaceAuthUtil {
    // 腾讯云API密钥配置
    @Value("${tencent.cloud.secretId}")
    private String secretId;

    @Value("${tencent.cloud.secretKey}")
    private String secretKey;

    @Value("${tencent.cloud.face.groupId}")
    private String groupId;

    @Value("${tencent.cloud.face.region}")
    private String region;

    /**
     * 人脸识别+活体检测，先进行人脸比对，如果比对成功了，验证是否活人拍照。
     * @param name 姓名
     * @param pid 身份证号
     * @param sex 性别
     * @param photo_1 身份证照片
     * @param photo_2 现场拍摄照片
     * @return true验证通过，false验证失败
     */
    public boolean verifyFaceModel(String name, String pid, String sex, String photo_1, String photo_2) {
        // 1.创建腾讯云客户端对象
        Credential cred = new Credential(secretId, secretKey);
        IaiClient client = new IaiClient(cred, region);

        // 2. 创建请求对象
        CompareFaceRequest compareFaceRequest = new CompareFaceRequest();
        compareFaceRequest.setImageA(photo_1);
        compareFaceRequest.setImageB(photo_2);

        // 3.发送请求，获取响应，解析响应
        CompareFaceResponse compareFaceResponse;
        try {
            compareFaceResponse = client.CompareFace(compareFaceRequest);
        } catch (TencentCloudSDKException e) {
            log.error("人脸比对失败，姓名：{}，身份证：{}", name, pid, e);
            throw new HisException("人脸比对失败");
        }

        // 4. 判断相似度
        Float score = compareFaceResponse.getScore();
        log.info("人脸比对分数，姓名：{}，身份证号：{}，分数：{}", name, pid, score);
        if(score < 50){
            log.warn("人脸比对分数过低，姓名:{}，身份证号：{}", name, pid);
            return false;
        }

        // 5. 活体检测（高精度版）
        DetectLiveFaceAccurateRequest detectLiveFaceAccurateRequest = new DetectLiveFaceAccurateRequest();
        detectLiveFaceAccurateRequest.setImage(photo_2);

        DetectLiveFaceAccurateResponse detectLiveFaceAccurateResponse;
        try {
            detectLiveFaceAccurateResponse = client.DetectLiveFaceAccurate(detectLiveFaceAccurateRequest);
        } catch (TencentCloudSDKException e) {
            log.error("活体检测失败，姓名：{}，身份证：{}", name, pid, e);
            throw new HisException("活体检测失败");
        }

        Float scoreDetect = detectLiveFaceAccurateResponse.getScore();
        if(scoreDetect < 40){
            log.warn("活体检测分数较低：{}，姓名：{}，身份证：{}", scoreDetect, name, pid);
            // 活体检测失败了
            return false;
        }

        // 6.同步人员库
        syncPersonToLibrary(client, name, pid, sex, photo_1);

        return true;
    }

    /**
     * 同步人员信息到腾讯云的人员库当中，先根据身份证号查询人员是否存在，如果不存在再调用录入人员库的接口完成录入。
     * @param client 调用接口的客户端对象
     * @param name 体检人的名字
     * @param pid 体检人的身份证号（PersonId）
     * @param sex 体检人的性别
     * @param photo1 体检人的身份证照片
     */
    private void syncPersonToLibrary(IaiClient client, String name, String pid, String sex, String photo1) {

        // 根据身份证号查询人员是否存在
        // 封装请求参数
        GetPersonBaseInfoRequest getPersonBaseInfoRequest = new GetPersonBaseInfoRequest();
        getPersonBaseInfoRequest.setPersonId(pid);

        GetPersonBaseInfoResponse getPersonBaseInfoResponse = null;
        try {
            // 发送请求根据PersonId查询人员库当中的信息。
            getPersonBaseInfoResponse = client.GetPersonBaseInfo(getPersonBaseInfoRequest);
        } catch (TencentCloudSDKException e) {
            // 人员不存在是正常情况，其他错误才需处理
            if (!"InvalidParameterValue.PersonIdNotExist".equals(e.getErrorCode())) {
                log.error("查询人员库失败 - 身份证: {}", pid, e);
                throw new HisException("查询人员库失败");
            }
        }

        // 判断 getPersonBaseInfoResponse 它是否为空，为空表示人员在人员库当中不存在。录入人员库。
        if(getPersonBaseInfoResponse == null){
            // 调用录入人员库的接口
            // 封装请求参数
            CreatePersonRequest createPersonRequest = new CreatePersonRequest();
            createPersonRequest.setGroupId(groupId);
            createPersonRequest.setPersonId(pid);
            createPersonRequest.setPersonName(name);
            createPersonRequest.setImage(photo1);
            createPersonRequest.setGender("男".equals(sex) ? 1L : 2L);
            createPersonRequest.setQualityControl(4L); // 最高值
            createPersonRequest.setUniquePersonControl(4L); // 最高值

            // 发送请求调用录入人员库的接口。
            try {
                CreatePersonResponse createPersonResponse = client.CreatePerson(createPersonRequest);
                String faceId = createPersonResponse.getFaceId();
                if(StrUtil.isBlank(faceId)){
                    log.error("录入人员库失败，FaceId为空，姓名：{}，身份证：{}", name, pid);
                    throw new HisException("录入到人员库失败");
                }
            } catch (TencentCloudSDKException e) {
                log.error("录入人员库失败，姓名：{}，身份证号：{}", name, pid, e);
                throw new HisException("添加人员库失败");
            }
        }else{
            // 人员在人员库当中已存在。
            log.info("人员在人员库当中已存在，姓名：{}，身份证：{}", name, pid);
        }
    }

}
