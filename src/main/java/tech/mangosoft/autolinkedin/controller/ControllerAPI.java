package tech.mangosoft.autolinkedin.controller;

public class ControllerAPI{

    //ACCOUNT
    static final String ACCOUNT_CONTROLLER = "/account";
    static final String BY_PASSWORD = "/password";

    //ADMIN
    static final String ADMIN_CONTROLLER = "/admin";
    static final String ALL_BY_PAGE = "/all/{page}";
    static final String CONFIRM = "/confirm/{confirmation}";

    //COMPANY
    static final String COMPANY_CONTROLLER = "/company";

    //ASSIGNMENT
    static final String ASSIGNMENT_CONTROLLER = "/assignment";
    static final String CREATE_GRABBING = "/createGrabbing";
    static final String CREATE_GRABBING_SALES = "/createGrabbingSales";
    static final String CREATE_CONNECTION = "/createConnection";
    static final String GET_STATISTICS = "/getStatistics";
    static final String GET_GROUPS = "/getGroups";
    static final String GET_INDUSTRIES = "/getIndustries";
    static final String GET_LOCATIONS = "/getLocations";
    static final String CHANGE_STATUS = "/changeStatus";
    static final String GET_CONNECTION_INFO_BY_ID_AND_PAGE = "/getConnectionInfo/{id}/{page}";
    static final String GET_ASSIGNMENT_BY_USER_AND_STATUS = "/getAssignmentByUserAndStatus";
    static final String GET_ASSIGNMENT_BY_PARAM = "/getAssignmentByParam";
    static final String GET_GRAPH_BY_TYPE = "/getGraphByType";

    //HEADCOUNT
    static final String HEADCOUNT_CONTROLLER = "/headcount";

    //CONTACT
    static final String CONTACT_CONTROLLER = "/contact";
    static final String GET_CONTACTS = "/getContacts";
    static final String GET_CONTACT_BY_ID = "/getContact/{id}";
    static final String UPDATE_CONTACT = "/update";
    static final String GET_CONTACTS_BY_STATUS = "/getContactsByStatus";
    static final String DOWNLOAD_FILE_BY_FILENAME = "/file/{filename}";
    static final String UPLOAD_FILE = "/upload";

    static final String ALL = "/all";
    static final String BY_ID = "/{id}";
    static final String BY_PAGE = "/{page}";
}