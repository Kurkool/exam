package com.ascendcorp.exam.service;

import com.ascendcorp.exam.model.InquiryServiceResultDTO;
import com.ascendcorp.exam.model.TransferResponse;
import com.ascendcorp.exam.proxy.BankProxyGateway;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.ws.WebServiceException;
import java.util.Date;

@Service
public class InquiryService {

    @Autowired
    private BankProxyGateway bankProxyGateway;

    final static Logger log = Logger.getLogger(InquiryService.class);

    public boolean ValidParam(String transactionId,
                                   Date tranDateTime,
                                   String channel,
                                   String bankCode,
                                   String bankNumber,
                                   double amount
                                   ){

        Boolean isValid = true;

        log.info("validate request parameters.");
        if(transactionId == null) {
            log.info("Transaction id is required!");
            isValid = false;
        }
        if(tranDateTime == null) {
            log.info("Transaction DateTime is required!");
            isValid = false;
        }
        if(channel == null) {
            log.info("Channel is required!");
            isValid = false;
        }
        if(bankCode == null || bankCode.equalsIgnoreCase("")) {
            log.info("Bank Code is required!");
            isValid = false;
        }
        if(bankNumber == null || bankNumber.equalsIgnoreCase("")) {
            log.info("Bank Number is required!");
            isValid = false;
        }
        if(amount <= 0) {
            log.info("Amount must more than zero!");
            isValid = false;
        }

        return isValid;
    }


    public InquiryServiceResultDTO inquiry(String transactionId,
                                           Date tranDateTime,
                                           String channel,
                                           String locationCode,
                                           String bankCode,
                                           String bankNumber,
                                           double amount,
                                           String reference1,
                                           String reference2,
                                           String firstName,
                                           String lastName)
    {
        InquiryServiceResultDTO respDTO = null;
        try
        {

            if(!ValidParam(transactionId, tranDateTime, channel, bankCode, bankNumber, amount)){
                throw new NullPointerException("Invalid or Missing value");
            }


            log.info("call bank web service");
            TransferResponse response = bankProxyGateway.requestTransfer(transactionId, tranDateTime, channel,
                    bankCode, bankNumber, amount, reference1, reference2);

            log.info("check bank response code");

            if(response != null) //New
            {
                log.debug("found response code");
                respDTO = new InquiryServiceResultDTO();
                respDTO.setRef_no1(response.getReferenceCode1());
                respDTO.setRef_no2(response.getReferenceCode2());
                respDTO.setAmount(response.getBalance());
                respDTO.setTranID(response.getBankTransactionID());

                String replyDesc;

                switch (response.getResponseCode().toLowerCase()){
                    case "approved":
                        respDTO.setReasonCode("200");
                        respDTO.setReasonDesc(response.getDescription());
                        respDTO.setAccountName(response.getDescription());
                        break;
                        //-------------
                    case "invalid_data":

                        // bank response code = invalid_data
                        replyDesc = response.getDescription();
                        if(replyDesc != null)
                        {
                            String respDesc[] = replyDesc.split(":");
                            if(respDesc != null && respDesc.length >= 3)
                            {
                                // bank description full format
                                respDTO.setReasonCode(respDesc[1]);
                                respDTO.setReasonDesc(respDesc[2]);
                            }else
                            {
                                // bank description short format
                                respDTO.setReasonCode("400");
                                respDTO.setReasonDesc("General Invalid Data");
                            }
                        }else
                        {
                            // bank no description
                            respDTO.setReasonCode("400");
                            respDTO.setReasonDesc("General Invalid Data");
                        }
                        break;
                        //-------------
                    case "transaction_error":
                        // bank response code = transaction_error
                        replyDesc = response.getDescription();
                        if(replyDesc != null)
                        {
                            String respDesc[] = replyDesc.split(":");


                            if(respDesc != null && respDesc.length >= 2)
                            {
                                log.info("Case Inquiry Error Code Format Now Will Get From [0] and [1] first");
                                String subIdx1 = respDesc[0];
                                String subIdx2 = respDesc[1];
                                log.info("index[0] : "+subIdx1 + " index[1] is >> "+subIdx2);
                                if("98".equalsIgnoreCase(subIdx1))
                                {
                                    // bank code 98
                                    respDTO.setReasonCode(subIdx1);
                                    respDTO.setReasonDesc(subIdx2);
                                }else
                                {
                                    log.info("case error is not 98 code");
                                    if(respDesc.length >= 3)
                                    {
                                        // bank description full format
                                        String subIdx3 = respDesc[2];
                                        log.info("index[0] : "+subIdx3);
                                        respDTO.setReasonCode(subIdx2);
                                        respDTO.setReasonDesc(subIdx3);
                                    }else
                                    {
                                        // bank description short format
                                        respDTO.setReasonCode(subIdx1);
                                        respDTO.setReasonDesc(subIdx2);
                                    }
                                }
                            }else
                            {
                                // bank description incorrect format
                                respDTO.setReasonCode("500");
                                respDTO.setReasonDesc("General Transaction Error");
                            }
                        }else
                        {
                            // bank no description
                            respDTO.setReasonCode("500");
                            respDTO.setReasonDesc("General Transaction Error");
                        }
                        break;
                    //-------------
                    case "unknown":
                        replyDesc = response.getDescription();
                        if(replyDesc != null)
                        {
                            String respDesc[] = replyDesc.split(":");
                            if(respDesc != null && respDesc.length >= 2)
                            {
                                // bank description full format
                                respDTO.setReasonCode(respDesc[0]);
                                respDTO.setReasonDesc(respDesc[1]);
                                if(respDTO.getReasonDesc() == null || respDTO.getReasonDesc().trim().length() == 0)
                                {
                                    respDTO.setReasonDesc("General Invalid Data");
                                }
                            }else
                            {
                                // bank description short format
                                respDTO.setReasonCode("501");
                                respDTO.setReasonDesc("General Invalid Data");
                            }
                        }else
                        {
                            // bank no description
                            respDTO.setReasonCode("501");
                            respDTO.setReasonDesc("General Invalid Data");
                        }
                        break;
                    //-------------
                    default: throw new Exception("Unsupport Error Reason Code");
                }

            }else
                // no resport from bank
                throw new Exception("Unable to inquiry from service.");
        }catch(NullPointerException ne)
        {
            if(respDTO == null)
            {
                respDTO = new InquiryServiceResultDTO();
                respDTO.setReasonCode("500");
                respDTO.setReasonDesc("General Invalid Data");
            }
        }catch(WebServiceException r)
        {
            // handle error from bank web service
            String faultString = r.getMessage();
            if(respDTO == null)
            {
                respDTO = new InquiryServiceResultDTO();
                if(faultString != null && (faultString.indexOf("java.net.SocketTimeoutException") > -1
                        || faultString.indexOf("Connection timed out") > -1 ))
                {
                    // bank timeout
                    respDTO.setReasonCode("503");
                    respDTO.setReasonDesc("Error timeout");
                }else
                {
                    // bank general error
                    respDTO.setReasonCode("504");
                    respDTO.setReasonDesc("Internal Application Error");
                }
            }
        }
        catch(Exception e)
        {
            log.error("inquiry exception", e);
            if(respDTO == null || (respDTO != null && respDTO.getReasonCode() == null))
            {
                respDTO = new InquiryServiceResultDTO();
                respDTO.setReasonCode("504");
                respDTO.setReasonDesc("Internal Application Error");
            }
        }
        return respDTO;
    }
}
