package com.walmart.truckmgt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * PageController – serves Thymeleaf HTML pages.
 * Data is loaded via JavaScript fetch() from the REST API endpoints.
 */
@Controller
public class PageController {

    /** Dashboard – home page */
    @GetMapping("/")
    public String dashboard() {
        return "dashboard";
    }

    /** Parking slot map */
    @GetMapping("/parking")
    public String parking() {
        return "parking";
    }

    /** Truck management table */
    @GetMapping("/trucks")
    public String trucks() {
        return "trucks";
    }

    /** Dispatch operations */
    @GetMapping("/dispatch")
    public String dispatch() {
        return "dispatch";
    }

    /** Warehouse gates live status */
    @GetMapping("/gates")
    public String gates() {
        return "gates";
    }

    /** Cleaning queue and history */
    @GetMapping("/cleaning")
    public String cleaning() {
        return "cleaning";
    }

    /** Reports and analytics */
    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }

    /** Compatibility rules matrix */
    @GetMapping("/rules")
    public String rules() {
        return "rules";
    }
}
