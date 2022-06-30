// ----------------------------------------------------------------------------
// Copyright 2007-2020, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  Public interface to OAUTH access-token retrieval.
// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.io.IOException;

public interface OAuth2AccessToken
{

    /**
    *** Retrieves an access-token from the specified provider OAUTH2 service, given
    *** the specified credentials.
    **/
    public String getAccessToken(String providerURL, String clientID, String secret, String refreshToken) 
        throws IOException;

    /**
    *** Sets the access-token as expired.
    *** Calling this method sets the access-token for the specified credentials to 
    *** be expired, forcing the OAUTH2 provider to renew/refresh the access-token 
    *** on the next call to <code>getAccessCode</code>.
    **/
    public void setAccessTokenExpired(String providerURL, String clientID)
        throws IOException;

}
