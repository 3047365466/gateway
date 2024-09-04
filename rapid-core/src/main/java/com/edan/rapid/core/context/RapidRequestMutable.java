package com.edan.rapid.core.context;

import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;

/**
 * <B>主类名称：</B>RapidRequestMutable<BR>
 * <B>概要说明：</B>请求可修改的参数操作接口<BR>
 * @author JiFeng
 * @since 2021年12月9日 上午11:51:20
 */
public interface RapidRequestMutable {

	/**
	 * <B>方法名称：</B>setModifyHost<BR>
	 * <B>概要说明：</B>设置请求host<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:51:54
	 * @param host
	 */
	void setModifyHost(String host);
	
	/**
	 * <B>方法名称：</B>getModifyHost<BR>
	 * <B>概要说明：</B>获取修改的host<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:59:40
	 */
	String getModifyHost();
	
	/**
	 * <B>方法名称：</B>setModifyPath<BR>
	 * <B>概要说明：</B>设置请求路径<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:52:31
	 * @param path
	 */
	void setModifyPath(String path);
	
	/**
	 * <B>方法名称：</B>getModifyPath<BR>
	 * <B>概要说明：</B>获取修改的地址<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 下午12:00:17
	 * @return
	 */
	String getModifyPath();
	
	/**
	 * <B>方法名称：</B>addHeader<BR>
	 * <B>概要说明：</B>添加请求头信息<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:53:06
	 * @param name
	 * @param value
	 */
	void addHeader(CharSequence name, String value);
	
	/**
	 * <B>方法名称：</B>setHeader<BR>
	 * <B>概要说明：</B>设置请求头信息<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:53:49
	 * @param name
	 * @param value
	 */
	void setHeader(CharSequence name, String value);
	
	/**
	 * <B>方法名称：</B>addQueryParam<BR>
	 * <B>概要说明：</B>添加请求的查询参数<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:54:32
	 * @param name
	 * @param value
	 */
	void addQueryParam(String name, String value);
	
	
	/**
	 * <B>方法名称：</B>addOrReplaceCookie<BR>
	 * <B>概要说明：</B>添加或替换cookie<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:55:49
	 * @param cookie
	 */
	void addOrReplaceCookie(Cookie cookie);
	
	/**
	 * <B>方法名称：</B>addFormParam<BR>
	 * <B>概要说明：</B>添加form表单参数<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:56:22
	 * @param name
	 * @param value
	 */
	void addFormParam(String name, String value);
	
	
	/**
	 * <B>方法名称：</B>setRequestTimeout<BR>
	 * <B>概要说明：</B>设置请求超时时间<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:57:11
	 * @param requestTimeout
	 */
	void setRequestTimeout(int requestTimeout);
	
	/**
	 * <B>方法名称：</B>build<BR>
	 * <B>概要说明：</B>构建转发请求的请求对象<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:58:00
	 * @return AsyncHttpClient Request
	 */
	Request build();
	
	/**
	 * <B>方法名称：</B>getFinalUrl<BR>
	 * <B>概要说明：</B>获取最终的路由路径<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午11:58:39
	 * @return
	 */
	String getFinalUrl();
	
}
