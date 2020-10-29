package com.ascendcorp.exam.service;

import com.ascendcorp.exam.model.InquiryServiceResultDTO;
import com.ascendcorp.exam.model.TransferResponse;
import com.ascendcorp.exam.proxy.BankProxyGateway;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.ws.WebServiceException;
import java.sql.SQLException;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InquiryServiceTest {

    @InjectMocks
    InquiryService inquiryService;

    @Mock
    BankProxyGateway bankProxyGateway;

    @Test
    public void should_return500_when_noRequireValue() throws SQLException {

        // Transaction Id
        InquiryServiceResultDTO inquiry = inquiryService.inquiry(null, new Date(),
                "Mobile", null,
                "BANK1", "4321000"
                , 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("500", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());

        // Datetime
        inquiry = inquiryService.inquiry("1234", null,
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("500", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());

        // Channel
        inquiry = inquiryService.inquiry("1234", new Date(),
                null, null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("500", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());

        // BankCode
        inquiry = inquiryService.inquiry("1234", new Date(),
                "Mobile", null,
                null, "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("500", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());

        // BankNumber
        inquiry = inquiryService.inquiry("1234", new Date(),
                "Mobile", null,
                "BANK1", null, 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("500", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());

        // Amount
        inquiry = inquiryService.inquiry("1234", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 0d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("500", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());
    }


    @Test
    public void should_return200_when_bankApproved() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("approved");
        transferResponse.setDescription("approved");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);


        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("200", inquiry.getReasonCode());
        assertEquals("approved", inquiry.getReasonDesc());
    }

    @Test
    public void should_return400_when_invalidDataWithoutDesc() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("invalid_data");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);


        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("400", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());
    }


    @Test
    public void should_return1091WithReasonDesc_when_invalidDataWithDescAndCode() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("invalid_data");
        transferResponse.setDescription("100:1091:Data type is invalid.");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);


        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("1091", inquiry.getReasonCode());
        assertEquals("Data type is invalid.", inquiry.getReasonDesc());
    }

    @Test
    public void should_return400_when_invalidDataWithDesc() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("invalid_data");
        transferResponse.setDescription("General error.");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);


        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("400", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());
    }

    @Test
    public void should_return400_when_errorAndDescIsNull() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("transaction_error");


        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);


        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("500", inquiry.getReasonCode());
        assertEquals("General Transaction Error", inquiry.getReasonDesc());
    }

    @Test
    public void should_return400_when_errorAndNoDescCode() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("transaction_error");
        transferResponse.setDescription("Transaction error.");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", "aasdf", null);

        assertNotNull(inquiry);
        assertEquals("500", inquiry.getReasonCode());
        assertEquals("General Transaction Error", inquiry.getReasonDesc());
    }

    @Test
    public void should_return400_when_errorAndDesc3Code() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("transaction_error");
        transferResponse.setDescription("100:1091:Transaction is error with code 1091.");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("1091", inquiry.getReasonCode());
        assertEquals("Transaction is error with code 1091.", inquiry.getReasonDesc());
    }

    @Test
    public void should_return400_when_errorAndDesc2Code() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("transaction_error");
        transferResponse.setDescription("1092:Transaction is error with code 1092.");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("1092", inquiry.getReasonCode());
        assertEquals("Transaction is error with code 1092.", inquiry.getReasonDesc());
    }

    @Test
    public void should_return400_when_errorAndDescCode98() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("transaction_error");
        transferResponse.setDescription("98:Transaction is error with code 98.");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("98", inquiry.getReasonCode());
        assertEquals("Transaction is error with code 98.", inquiry.getReasonDesc());
    }

    @Test
    public void should_return501_when_unknownAndWithoutDesc() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("unknown");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("501", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());
    }

    @Test
    public void should_return501_when_unknownAndDesc() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("unknown");
        transferResponse.setDescription("5001:Unknown error code 5001");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("5001", inquiry.getReasonCode());
        assertEquals("Unknown error code 5001", inquiry.getReasonDesc());
    }

    @Test
    public void should_return501_when_unknownAndEmptyDesc() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("unknown");
        transferResponse.setDescription("5002: ");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("5002", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());
    }

    @Test
    public void should_return501_when_unknownAndTextDesc() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("unknown");
        transferResponse.setDescription("General Invalid Data code 501");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("501", inquiry.getReasonCode());
        assertEquals("General Invalid Data", inquiry.getReasonDesc());
    }

    @Test
    public void should_return504_when_errorDescNotSupport() throws SQLException {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("not_support");
        transferResponse.setDescription("Not support");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(transferResponse);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("504", inquiry.getReasonCode());
        assertEquals("Internal Application Error", inquiry.getReasonDesc());
    }

    @Test
    public void should_return504_when_responseNull() throws SQLException {

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenReturn(null);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("504", inquiry.getReasonCode());
        assertEquals("Internal Application Error", inquiry.getReasonDesc());
    }

    @Test
    public void should_return503_when_throwWebServiceException() throws SQLException {

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenThrow(WebServiceException.class);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("504", inquiry.getReasonCode());
        assertEquals("Internal Application Error", inquiry.getReasonDesc());
    }

    @Test
    public void should_return503_when_socketTimeout() throws SQLException {

        WebServiceException ex = new WebServiceException("java.net.SocketTimeoutException error");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenThrow(ex);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("503", inquiry.getReasonCode());
        assertEquals("Error timeout", inquiry.getReasonDesc());
    }

    @Test
    public void should_return503_when_connectionTimeout() throws SQLException {

        WebServiceException ex = new WebServiceException("Server Connection timed out");

        when(bankProxyGateway.requestTransfer(anyString(),any(),anyString(),anyString(),anyString(),
                anyDouble(),anyString(),anyString())).thenThrow(ex);

        InquiryServiceResultDTO inquiry = inquiryService.inquiry("123456", new Date(),
                "Mobile", null,
                "BANK1", "4321000", 100d, "rrivsffv234c",
                "11223xfgt", null, null);

        assertNotNull(inquiry);
        assertEquals("503", inquiry.getReasonCode());
        assertEquals("Error timeout", inquiry.getReasonDesc());
    }
}
