package org.transmartproject.db.log

import grails.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.shared.SecurityService
import org.transmartproject.core.log.AccessLogEntryResource
import org.transmartproject.core.users.User

class AccessLogService implements AccessLogEntryResource {

	@Autowired private SecurityService securityService

	@Transactional
	AccessLogEntry report(Map<String, Object> additionalParams = [:], User user, String event) {
		new AccessLogEntry(
				username:     user?.username,
				event:        event,
				eventMessage: additionalParams?.eventMessage,
				requestURL:   additionalParams?.requestURL,
				accessTime:   additionalParams?.accessTime ?: new Date(),
		).save()
	}

	@Transactional
	AccessLogEntry report(String username = securityService.currentUsername(), String event, String message) {
		new AccessLogEntry(username: username, event: event, eventMessage: message, accessTime: new Date()).save()
	}

	List<AccessLogEntry> listEvents(Map<String, Object> paginationParams = [:], Date startDate, Date endDate) {
		AccessLogEntry.createCriteria().list(
				max:    paginationParams?.max,
				offset: paginationParams?.offset,
				sort:   paginationParams?.sort,
				order:  paginationParams?.order) {

			if (startDate && endDate) {
				between 'accessTime', startDate, endDate
			}
			else if (startDate) {
				gte 'accessTime', startDate
			}
			else if (endDate) {
				lte 'accessTime', endDate
			}
		}
	}
}
