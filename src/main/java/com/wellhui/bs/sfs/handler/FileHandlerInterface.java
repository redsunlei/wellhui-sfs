package com.wellhui.bs.sfs.handler;


/**
 * �ļ�������ӿ�
 *
 * @author ����
 * @version 2014-11-23 ����
 * @since 1.0
 */
public interface FileHandlerInterface {

    //�Ƿ���
    boolean check(String uri);

    // �Ƿ���Ҫ����Ʊ��
    boolean security();

    //�Ƿ񻺴�
    boolean cacheable();

    //�ļ�·��
    String path(String uri);

    //�ļ�Ĭ��·��
    String defaultPath(String uri);

    //�ļ���������
    String filename(String uri);

}
