#ifndef TERMINAL_SERVICE_INTERFACE_H
#define TERMINAL_SERVICE_INTERFACE_H

#ifdef __cplusplus
extern "C"
{
#endif

/*
 * The function get credit card statistic(total_count or failed_count)
 * @param[in]		: int card_type : enum CARD_TYPE
 * @param[in]		: int count_type : enum COUNT_TYPE
 * @param[out]		: unsigned int* count : statistic count data
 * @return value	: < 0 : error
 * 					  = 0 : success
 */
typedef int (*GET_CARD_STATISTIC)(int card_type, int count_type, unsigned int* count);


#ifdef __cplusplus
}
#endif


#endif
