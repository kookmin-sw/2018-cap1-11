package com.c.myapplication.remote;

import com.c.myapplication.item.MemberInfoItem;
//import com.c.myapplication.item.treeInfoItem;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;



public interface RemoteService {

    String BASE_URL ="http://13.209.43.188:3000";
    String MEMBER_ICON_URL = BASE_URL + "/member/";
    String IMAGE_URL = BASE_URL + "/img";

    @GET("/member/{phone}")
    Call<MemberInfoItem> selectMemberInfo(@Path("phone") String phone);

    @POST("/member/info")
    Call<String> insertMemberInfo(@Body MemberInfoItem memberInfoItem);

    @FormUrlEncoded
    @POST("/member/phone")
    Call<String> insertMemberPhone(@Field("phone") String phone);

    @Multipart
    @POST("/member/icon_upload")
    Call<ResponseBody> uploadMemberIcon(@Part("member_seq") RequestBody memberSeq,
                                        @Part MultipartBody.Part file);
/*
    //나무무 정보
    @GET("/tree/info/{info_seq}")
    Call<treeInfoItem> selecttreeInfo(@Path("info_seq") int treeInfoSeq,
                                      @Query("member_seq") int memberSeq);

    @POST("/tree/info")
    Call<String> inserttreeInfo(@Body treeInfoItem infoItem);

*/

}
