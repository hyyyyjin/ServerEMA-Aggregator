package com.mir.ems.coap.client;

import java.sql.Date;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mir.ems.globalVar.global;

public class Poll_Periodic extends Thread {

	JSONObject json;

	String pathSet = "coap://" + global.coapServerIP + ":" + global.coapServerPort + "/EMAP/"
			+ global.getParentnNodeID() + "/" + global.version + "/";

	String openADRpathSet = "coap://" + global.coapServerIP + ":" + global.coapServerPort + "/OpenADR/"
			+ global.ParentnNodeID + "/" + global.openADRVersion + "/";

	boolean eventFlag = true;

	public Poll_Periodic() {
//		this.controller = controller;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Timer timer = new Timer();
		timer.schedule(new UpdateTask(), 0, global.httpPollingInterval);
	}

	private class UpdateTask extends TimerTask {

		@Override
		public void run() {

			if (eventFlag) {
//				global.profile.equals("EMAP1.0b")
				if (global.profile.equals("EMAP1.0b")) {
					if (global.pullModel) {
						
						com.mir.ems.profile.emap.v2.Poll poll = new com.mir.ems.profile.emap.v2.Poll();
						poll.setDestEMA(global.getParentnNodeID());
						poll.setService("Poll");
						poll.setSrcEMA(global.CHILD_ID);
						poll.setTime(new Date(System.currentTimeMillis()).toString());

						String uri = pathSet + "Poll";
						CoapClient client = new CoapClient();

						client.setURI(uri);

						client.put(new CoapHandler() {

							@Override
							public void onLoad(CoapResponse response) {
								// TODO Auto-generated method stub
								String content = response.getResponseText();
								String eventID = "";
								try {
									json = new JSONObject(content);

									String responseDescription = json.getString("service");

									if (responseDescription.matches("Response")) {

									} else if (responseDescription.matches("DistributeEvent")) {

										double threshold = 0;

										JSONArray jsonArr = new JSONArray(json.getString("event"));

										for (int i = 0; i < jsonArr.length(); i++) {

											JSONObject subJson = new JSONObject(jsonArr.get(i).toString());
											eventID  = subJson.getString("eventID");
											JSONArray subJsonArr = new JSONArray(subJson.getString("eventSignals"));

											for (int j = 0; j < subJsonArr.length(); j++) {

												JSONObject subJson2 = new JSONObject(subJsonArr.get(i).toString());

												threshold = subJson2.getDouble("threshold");
											}

										}


//										controller.setThreshold(threshold);
//					
										System.out.println("==================================");
										System.out.println("EMA ID" + global.CHILD_ID);
										System.out.println("EVENT RECV" + threshold);
										System.out.println("CURRENT VAL" + global.currentVal);
										System.out.println("==================================");
//										controller.setReportCnt(4);
										eventFlag = false;
										CreatedEvent(eventID);
									}

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}

							@Override
							public void onError() {
								// TODO Auto-generated method stub
							}

						}, poll.toString(), MediaTypeRegistry.APPLICATION_JSON);

					}

					
				} else if (global.profile.equals("OpenADR2.0b")) {

					if (global.pullModel) {

//						com.mir.ems.profile.openadr.recent
						com.mir.ems.profile.openadr.recent.Poll poll = new com.mir.ems.profile.openadr.recent.Poll();

						poll.setService("OadrPoll");
						poll.setVenID(global.CHILD_ID);

						String uri = openADRpathSet + "OadrPoll";
						CoapClient client = new CoapClient();

						client.setURI(uri);
						CoapResponse resp = client.put(poll.toString(), MediaTypeRegistry.APPLICATION_JSON);

						try {

							json = new JSONObject(resp.getResponseText().toString());
							String responseDescription = json.getString("service");

							if (responseDescription.matches("oadrResponse")) {
								Thread.sleep(global.interval);
								// UpdateReport();
								// Poll_Periodical();
							} else {

								double threshold = 0;

								JSONArray jsonArr = new JSONArray(json.getString("event"));

								for (int i = 0; i < jsonArr.length(); i++) {

									JSONObject subJson = new JSONObject(jsonArr.get(i).toString());

									JSONArray subJsonArr = new JSONArray(subJson.getString("eventSignals"));

									for (int j = 0; j < subJsonArr.length(); j++) {

										JSONObject subJson2 = new JSONObject(subJsonArr.get(i).toString());

										JSONArray subJsonArr2 = new JSONArray(subJson2.getString("intervals"));

										for (int k = 0; k < subJsonArr2.length(); k++) {

											JSONObject subJson3 = new JSONObject(subJsonArr2.get(i).toString());

											threshold = subJson3.getDouble("value");

										}
									}

								}

//								controller.setThreshold(threshold);
								global.THRESHOLD= threshold;
								// System.out.println("==================================");
								// System.out.println("EMA ID"+
								// global.CHILD_ID);
								// System.out.println("EVENT RECV"+ threshold);
								// System.out.println("CURRENT VAL"+
								// controller.getCurrentPower());
								// System.out.println("==================================");

								System.out.println(threshold);
//								controller.setReportCnt(4);

								CreatedEvent("eventID");
							}

						} catch (Exception e) {
							e.printStackTrace();
							// Poll_Periodical();
						}

					}

					// else if (observeFlag == 0) {
					//
					// CoapClient client = new CoapClient();
					//
					// String uri = "coap://" + global.coapServerIP + ":" +
					// global.coapServerPort + "/OpenADR2.0b/"
					// + global.CHILD_ID;
					// client.setURI(uri);
					// client.observe(new CoapHandler() {
					//
					// @Override
					// public void onLoad(CoapResponse response) {
					// String content = response.getResponseText();
					//
					// if (content.contains("DistributeEvent")) {
					//
					// double threshold = 0;
					//
					// JSONArray jsonArr;
					// try {
					// json = new JSONObject(content);
					//
					// jsonArr = new JSONArray(json.getString("event"));
					//
					// for (int i = 0; i < jsonArr.length(); i++) {
					//
					// JSONObject subJson = new
					// JSONObject(jsonArr.get(i).toString());
					//
					// JSONArray subJsonArr = new
					// JSONArray(subJson.getString("eventSignals"));
					//
					// for (int j = 0; j < subJsonArr.length(); j++) {
					//
					// JSONObject subJson2 = new
					// JSONObject(subJsonArr.get(i).toString());
					//
					// JSONArray subJsonArr2 = new
					// JSONArray(subJson2.getString("intervals"));
					//
					// for (int k = 0; k < subJsonArr2.length(); k++) {
					//
					// JSONObject subJson3 = new
					// JSONObject(subJsonArr2.get(i).toString());
					//
					// threshold = subJson3.getDouble("value");
					//
					// }
					// }
					//
					// }
					//
					// controller.setThreshold(threshold);
					// //
					// System.out.println("==================================");
					// // System.out.println("EMA ID"+
					// // global.CHILD_ID);
					// // System.out.println("EVENT RECV"+ threshold);
					// // System.out.println("CURRENT VAL"+
					// // controller.getCurrentPower());
					// //
					// System.out.println("==================================");
					// controller.setReportCnt(4);
					// CreatedEvent();
					//
					// System.err.println("threshold" + threshold);
					// } catch (JSONException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					//
					// observeFlag = 1;
					// }
					// }
					//
					// @Override
					// public void onError() {
					// System.err.println("OBSERVING FAILED (press enter to
					// exit)");
					// }
					//
					// });
					//
					// }

				}

			}
		}
	}

	public void CreatedEvent(String eventID) {
		if (global.profile.equals("EMAP1.0b")) {

			com.mir.ems.profile.emap.v2.CreatedEvent cde = new com.mir.ems.profile.emap.v2.CreatedEvent();

			cde.setDestEMA(global.getParentnNodeID());
			cde.setEventID(eventID);
			cde.setModificationNumber(1);
			cde.setOptType("optIn");
			cde.setRequestID("requestID");
			cde.setResponseCode(200);
			cde.setResponseDescription("OK");
			cde.setService("CreatedEvent");
			cde.setSrcEMA(global.CHILD_ID);
			cde.setTime(new Date(System.currentTimeMillis()).toString());

			String uri = pathSet + "Event";
			CoapClient client = new CoapClient();

			client.setURI(uri);
			
			client.put(new CoapHandler(){
				
				
				
				@Override
				public void onLoad(CoapResponse response) {
					// TODO Auto-generated method stub
					try {

						json = new JSONObject(response.getResponseText().toString());

						String responseDescription = json.getString("service");

						// 하위 Device에게 이벤트 발생 - 2018-12-14
						Calendar calendar = Calendar.getInstance();
						int year = calendar.get(Calendar.YEAR);
						int month = calendar.get(Calendar.MONTH) + 1;
						int day = calendar.get(Calendar.DATE);

						int hour = calendar.get(Calendar.HOUR_OF_DAY);
						int minute = calendar.get(Calendar.MINUTE);
						int e_minute = calendar.get(Calendar.MINUTE) + 1;

						String sYMD = year + "" + month + "" + day + "";
						String sTime = hour + "" + minute + "" + e_minute + "";

						String eventDest = "DEVICE1";
						if (global.emaProtocolCoAP_EventFlag.containsKey(eventDest)) {

							if (global.emaProtocolCoAP.get(eventDest).isPullModel()) {
								global.emaProtocolCoAP_EventFlag.get(eventDest).setEventFlag(true)
										.setStartYMD(Integer.parseInt(sYMD))
										.setStartTime(Integer.parseInt(sTime + "11")).setEndYMD(Integer.parseInt(sYMD))
										.setEndTime(Integer.parseInt(sTime + "11"))
										.setThreshold(Double.parseDouble(100 + "")).setEventID(eventID);;
							} else {
								System.err.println(eventDest + " is not supported Pull Model");
								return;
							}

						}
						global.eventFromServer = true;

						if (responseDescription.matches("Response")) {
							// Poll_Periodical();
							eventFlag = true;
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				public void onError() {};
			}, cde.toString(), MediaTypeRegistry.APPLICATION_JSON);
			
		} else if (global.profile.equals("OpenADR2.0b")) {

			com.mir.ems.profile.openadr.recent.CreatedEvent cde = new com.mir.ems.profile.openadr.recent.CreatedEvent();

			cde.setVtnID(global.getParentnNodeID());
			cde.setEventID("eventID");
			cde.setModificationNumber(1);
			cde.setOptType("optIn");
			cde.setRequestID("requestID");
			cde.setResponseCode(200);
			cde.setResponseDescription("OK");
			cde.setService("oadrCreatedEvent");
			cde.setVenID(global.CHILD_ID);

			String uri = openADRpathSet + "EiEvent";
			CoapClient client = new CoapClient();

			client.setURI(uri);
			CoapResponse resp = client.put(cde.toString(), MediaTypeRegistry.APPLICATION_JSON);

			try {

				json = new JSONObject(resp.getResponseText().toString());

				String responseDescription = json.getString("service");

				if (responseDescription.matches("oadrResponse")) {
					eventFlag = true;
					// Poll_Periodical();
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

}
